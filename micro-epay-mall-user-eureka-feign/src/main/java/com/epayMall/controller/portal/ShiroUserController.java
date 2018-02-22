package com.epayMall.controller.portal;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.epayMall.common.Const;
import com.epayMall.common.ResponseCodeEnum;
import com.epayMall.common.ServerResponse;
import com.epayMall.component.shiro.MySessionDao;
import com.epayMall.config.MyShiroRealm;
import com.epayMall.dao.UserMapper;
import com.epayMall.pojo.User;


@RestController
@RequestMapping("/epay-mall/user")
@ConfigurationProperties(prefix="shiroFilter")
public class ShiroUserController {
	
	private static final Logger logger = LoggerFactory.getLogger(ShiroUserController.class);
	private static final String CONTROLLER = "ShiroUserController";
	
	@Autowired
	private MyShiroRealm myShiroRealm;
	@Autowired
	private MySessionDao mySessionDao;
	@Autowired
	private UserMapper userMapper;
	
	@PostMapping("/remote/getUserByUsername")
	public ServerResponse<User> getUserByUsername(String username) {
		User user = userMapper.selectByUserName(username);
		return ServerResponse.createBySucessResReturnData(user);
	}

	@RequestMapping(value="/userLogin", method=RequestMethod.POST)
	public ServerResponse<User> userLogin(String username, String password) {
		logger.info("[{}]-[userLogin method]-[接受客户端请求]", CONTROLLER);
		
		Subject currentUser = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(username, password);
		token.setRememberMe(true);
		
		boolean flag = true;
		String incorrectlyMsg = StringUtils.EMPTY;
		try {
            currentUser.login(token);
        } catch (UnknownAccountException uae) {
        		logger.error("There is no user with username of " + token.getPrincipal());
        		flag = false;
        		incorrectlyMsg = "该用户未注册！";
        } catch (IncorrectCredentialsException ice) {
        		logger.error("Password for account {} was incorrect!",token.getPrincipal());
        		flag = false;
        		incorrectlyMsg = "密码错误！";
        } catch (LockedAccountException lae) {
        		logger.error("The account for username {} is locked. Please contact your administrator to unlock it.", token.getPrincipal() );
        		flag = false;
        		incorrectlyMsg = "该用户已被冻结！";
        }catch (AuthenticationException ae) {
            logger.error("AuthenticationException, {}", ae);
            flag = false;
            incorrectlyMsg = "服务器异常，请稍后重试！";
        }
		
		if(flag) {
			User user = (User) currentUser.getPrincipal();
			Session session = currentUser.getSession();
			updateUserSession(session, user);
			logger.info("{}用户成功登陆", user.getUsername());
			return ServerResponse.createBySucessResReturnData(user);
		} else {
			logger.info("未能成功登陆");
			return ServerResponse.createByErrorResReturnMsg(incorrectlyMsg);
		}
	}
	
	@RequestMapping(value="/get/authenticationInfo", method=RequestMethod.POST)
	public ServerResponse<AuthenticationInfo> getAuthenticationInfo(AuthenticationToken token) {
		AuthenticationInfo authenticationInfo = myShiroRealm.doGetAuthenticationInfoService(token);
		return ServerResponse.createBySucessResReturnData(authenticationInfo);
	}
	
	@PostMapping("/get/userSession")
	public ServerResponse<Session> getUserSession(Session session) {
		logger.info("[{}]-[getUserSession method]-[接受客户端请求]", CONTROLLER);
		if(session == null) {
			return ServerResponse.createBySucessResReturnData(null);
		}
 		Serializable sessionId = session.getId();
		
		Session realSession = mySessionDao.readSession(sessionId);
		return ServerResponse.createBySucessResReturnData(realSession);
	}
	
	@PostMapping("/update/userSession")
	public void updateUserSession(Session session, User user) {
		logger.info("[{}]-[updateUserSession method]-[接受客户端请求]", CONTROLLER);
		session.setAttribute(Const.CURRENT_USER, user);
		mySessionDao.update(session);
	}
	
	@DeleteMapping("/delete/userSession")
	public void deleteUserSession(Session session) {
		logger.info("[{}]-[deleteUserSession method]-[接受客户端请求]", CONTROLLER);
		session.setAttribute(Const.CURRENT_USER, null);
		mySessionDao.delete(session);
	}
	
	@GetMapping("/remote/userLogout")
	public ServerResponse<String> remoteUserLogout(Session session) {
		logger.info("[{}]-[remoteUserLogout method]-[接受客户端请求]", CONTROLLER);
		deleteUserSession(session);
		return ServerResponse.createBySucessResReturnMsg("用户成功退出登陆");
	}
	
	@GetMapping("/userLogout")
	public ServerResponse<String>UserLogout() {
		logger.info("[{}]-[userLogout method]-[接受客户端请求]", CONTROLLER);
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		return remoteUserLogout(session);
	}
	
	@GetMapping("/get/personalInformation")
	public <T> ServerResponse<T> getPersonalInformation() {
		logger.info("[{}]-[getPersonalInformation method]-[接受客户端请求]", CONTROLLER);
		Subject currentSubject = SecurityUtils.getSubject();
		Session session = currentSubject.getSession();
		
		return getRemotePersonalInformation(session);
	}
	
	@GetMapping("/remote/get/personalInformation")
	public <T> ServerResponse<T> getRemotePersonalInformation(Session session) {
		logger.info("[{}]-[getRemotePersonalInformation method]-[接受客户端请求]", CONTROLLER);
		ServerResponse response;
		//先校验用户是否已经登陆
		User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
		if (currentUser == null) {
			response = ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),"未登录,需要强制登录status=10");
			logger.info("[{}]-[getRemotePersonalInformation method]-[获取个人信息强制登陆失败,响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		}
		
		currentUser.setPassword("");
		response = ServerResponse.createBySucessResReturnData(currentUser);
		logger.info("[{}]-[getRemotePersonalInformation method]-[用户 {} 获取个人信息强制登陆成功,响应描述为：{}]", CONTROLLER,
				currentUser.getUsername(), response.getMsg());
		return response;
	}
	
	
	@GetMapping("/remote/get/userInfo")
	public ServerResponse<User> getRemoteUserInfo(Session session){
		logger.info("[{}]-[getUserInfo method]-[接受客户端请求]", CONTROLLER);
		
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			ServerResponse<User> response = ServerResponse.createByErrorResReturnMsg("用户未登录,无法获取当前用户信息");
			logger.info("[{}]-[getUserInfo method]-[获取用户信息,响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		}
		
		ServerResponse<User> response = ServerResponse.createBySucessResReturnMsgAndData("成功获取用户信息", user);
		logger.info("[{}]-[getUserInfo method]-[获取用户信息,响应描述为：{}]", CONTROLLER, response.getMsg());
		return response;
	}
	
}
