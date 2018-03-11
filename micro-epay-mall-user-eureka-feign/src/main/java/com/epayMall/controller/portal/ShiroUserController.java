package com.epayMall.controller.portal;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.epayMall.common.Const;
import com.epayMall.common.ResponseCodeEnum;
import com.epayMall.common.ServerResponse;
import com.epayMall.dao.UserMapper;
import com.epayMall.pojo.User;
import com.epayMall.redis.ICacheService;
import com.epayMall.util.CookieUtil;


@RestController
@RequestMapping("/epay-mall/user")
@ConfigurationProperties(prefix="shiroFilter")
public class ShiroUserController {
	
	private static final Logger logger = LoggerFactory.getLogger(ShiroUserController.class);
	private static final String CONTROLLER = "ShiroUserController";
	
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private ICacheService cacheService;
	
	@PostMapping("/remote/getUserByUsername")
	public ServerResponse<User> getUserByUsername(String username) {
		User user = userMapper.selectByUserName(username);
		return ServerResponse.createBySucessResReturnData(user);
	}

	@PostMapping(value="/userLogin")
	public ServerResponse<User> userLogin(HttpServletResponse response, String username, String password) {
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
			
			String customToken = CookieUtil.writeCustomCookie(response);
			cacheService.set(customToken, user, Const.REDIS_SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
			
			logger.info("{}用户成功登陆,且已经将用户信息保存至Redis中", user.getUsername());
			return ServerResponse.createBySucessResReturnData(user);
		} else {
			logger.info("未能成功登陆");
			return ServerResponse.createByErrorResReturnMsg(incorrectlyMsg);
		}
	}
	
	
	@GetMapping("/userLogout")
	public ServerResponse<String> remoteUserLogout(HttpServletRequest request, HttpServletResponse response) {
		logger.info("[{}]-[remoteUserLogout method]-[接受客户端请求]", CONTROLLER);
		CookieUtil.deleteCustomCookie(request, response);
		return ServerResponse.createBySucessResReturnMsg("用户成功退出登陆");
	}
	
	
	@GetMapping("/get/personalInformation")
	public ServerResponse<User> getRemotePersonalInformation(HttpServletRequest request) {
		logger.info("[{}]-[getRemotePersonalInformation method]-[接受客户端请求]", CONTROLLER);
		ServerResponse<User> response;
		//先校验用户是否已经登陆
		String customToken = CookieUtil.readCustomCookie(request);
		ServerResponse<User> userRes = checkUserIfHasCustomSessionId(customToken);
		if(!userRes.isSuccess()){
			logger.warn("[checkUserIfHasCustomSessionId]-have something wrong:{}", userRes.getMsg());
			return ServerResponse.createByErrorResReturnMsg(userRes.getMsg());
		}
		
		User currentUser = userRes.getData();
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
	
	
	@GetMapping("/get/userInfo")
	public ServerResponse<User> getRemoteUserInfo(HttpServletRequest request){
		logger.info("[{}]-[getUserInfo method]-[接受客户端请求]", CONTROLLER);
		
		String customToken = CookieUtil.readCustomCookie(request);
		ServerResponse<User> userRes = checkUserIfHasCustomSessionId(customToken);
		if(!userRes.isSuccess()){
			logger.warn("[checkUserIfHasCustomSessionId]-have something wrong:{}", userRes.getMsg());
			return ServerResponse.createByErrorResReturnMsg(userRes.getMsg());
		}
		
		User user = userRes.getData();
		if (user == null) {
			ServerResponse<User> response = ServerResponse.createByErrorResReturnMsg("用户未登录,无法获取当前用户信息");
			logger.info("[{}]-[getUserInfo method]-[获取用户信息,响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		}
		
		ServerResponse<User> response = ServerResponse.createBySucessResReturnMsgAndData("成功获取用户信息", user);
		logger.info("[{}]-[getUserInfo method]-[获取用户信息,响应描述为：{}]", CONTROLLER, response.getMsg());
		return response;
	}
	
	@PostMapping("/checkUserIfHasCustomSessionId")
	public ServerResponse<User> checkUserIfHasCustomSessionId(String customToken) {
		if(StringUtils.isBlank(customToken)) {
			return ServerResponse.createBySucessResReturnData(null);
		}
		
		User user = cacheService.get(customToken, User.class);
		if(user == null) {
			return ServerResponse.createBySucessResReturnData(null);
		}
		
		return ServerResponse.createBySucessResReturnData(user);
	}
	
	
}
