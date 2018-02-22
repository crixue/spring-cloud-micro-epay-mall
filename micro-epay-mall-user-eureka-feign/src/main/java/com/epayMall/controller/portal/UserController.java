package com.epayMall.controller.portal;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.epayMall.common.Const;
import com.epayMall.common.ResponseCodeEnum;
import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.User;
import com.epayMall.service.IUserService;

/**
 * 用户模块
 * @author crixus
 *
 */
@Controller
@RequestMapping("/epay-mall/user/")
public class UserController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	private static final String CONTROLLER = "UserController";
	
	@Autowired
	private IUserService iUserService;
	
	/**
	 * 用户登陆
	 * @param username
	 * @param password
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "login.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> userLogin(String username, String password, HttpSession session){
		logger.info("[{}]-[userLogin method]-[接受客户端请求]", CONTROLLER);
		//检验登陆用户的合法性
		ServerResponse<User> response = iUserService.checkUserLogin(username, password);
		if (!response.isSuccess()) {
			logger.info("[{}]-[userLogin method]-[为 {} 用户返回响应内容,响应描述为：{}]", CONTROLLER,
					username, response.getMsg());
			return response;
		}
		
		//将已登陆的用户存入到session中
		session.setAttribute(Const.CURRENT_USER, response.getData());
		logger.info("[{}]-[userLogin method]-[为 {} 用户返回响应内容,响应描述为：{}]", CONTROLLER,
				username, response.getMsg());
		return response;
	}
	
	/**
	 * 退出登陆
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "logout.do")
	@ResponseBody
	public ServerResponse<String> userLogout(HttpSession session){
		logger.info("[{}]-[userLogout method]-[接受客户端请求]", CONTROLLER);
		
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		ServerResponse<String> response = ServerResponse.createBySucessResReturnMsg("用户成功退出登陆");
		logger.info("[{}]-[userLogin method]-[为 {} 用户退出登陆,响应描述为：{}]", CONTROLLER,
				user.getUsername(), response.getMsg());
		session.setAttribute(Const.CURRENT_USER, null);
		return response;
	}
	
	/**
	 * 注册
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "register.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> register(User user){
		logger.info("[{}]-[register method]-[接受客户端请求]", CONTROLLER);
		ServerResponse<String> response = iUserService.register(user);
		logger.info("[{}]-[register method]-[为 {} 用户进行注册,响应描述为：{}]", CONTROLLER,
				user.getUsername(), response.getMsg());
		return response;
	}
	
	/**
	 * 通过邮箱或者短信注册
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "registerByAuthCode.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> registerByAuthCode(User user, String type, String authCode){
		logger.info("[{}]-[registerByAuthCode method]-[接受客户端请求]", CONTROLLER);
		
		ServerResponse<String> response = null;
		if (StringUtils.equals(type, Const.EMAIL)) {
			response = iUserService.registerBySendingEmailAuthCode(authCode, user);
		}
		
		logger.info("[{}]-[registerByAuthCode method]-[为 {} 用户进行注册,响应描述为：{}]", CONTROLLER,
				user.getUsername(), response.getMsg());
		return response;
	}

	/**
	 * 检验用户是否存在
	 * @param str
	 * @param type
	 * @return
	 */
	@RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> checkUserIfExists(String str, String type){
		logger.info("[{}]-[checkUserIfExists method]-[接受客户端请求]", CONTROLLER);
		ServerResponse<String> response = iUserService.checkValid(str, type);
		logger.info("[{}]-[checkUserIfExists method]-[验证 {} 类型名为 {} 用户是否存在,响应描述为：{}]", CONTROLLER,
				type, str, response.getMsg());
		return response;
	}
	
	/**
	 * 获取用户信息
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "get_user_info.do")
	@ResponseBody
	public ServerResponse getUserInfo(HttpSession session){
		logger.info("[{}]-[getUserInfo method]-[接受客户端请求]", CONTROLLER);
		
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			ServerResponse response = ServerResponse.createByErrorResReturnMsg("用户未登录,无法获取当前用户信息");
			logger.info("[{}]-[getUserInfo method]-[获取用户信息,响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		}
		
		ServerResponse response = ServerResponse.createBySucessResReturnMsgAndData("成功获取用户信息", user);
		logger.info("[{}]-[getUserInfo method]-[获取用户信息,响应描述为：{}]", CONTROLLER, response.getMsg());
		return response;
	}
	
	/**
	 * 获取用户的忘记密码的问题
	 * @param username
	 * @return
	 */
	@RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse getForgettenQuestion(String username){
		logger.info("[{}]-[getForgettenQuestion method]-[接受客户端请求]", CONTROLLER);
		ServerResponse response = iUserService.getForgettenQuestion(username);
		if (!response.isSuccess()) {
			logger.info("[{}]-[getForgettenQuestion method]-[未获取 {} 用户的忘记密码问题,响应描述为：{}]", CONTROLLER, 
					username, response.getMsg());
			return response;
		}
		logger.info("[{}]-[getForgettenQuestion method]-[成功获取 {} 用户的忘记密码问题]", CONTROLLER, username);
		return response;
	}
	
	/**
	 * 校验用户回答忘记密码的问题的正确性
	 * @param username
	 * @param question
	 * @param answer
	 * @return
	 */
	@RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> checkForgettenQuestionIsRight(String username, String question, String answer){
		logger.info("[{}]-[checkForgettenQuestionIsRight method]-[接受客户端请求]", CONTROLLER);
		ServerResponse<String> response = iUserService.checkAnswerIsRight(username, question, answer);
		if (!response.isSuccess()) {
			logger.info("[{}]-[checkForgettenQuestionIsRight method]-[用户 {} 未正确回答忘记密码问题,响应描述为：{}]", CONTROLLER, 
					username, response.getMsg());
			return response;
		}
		logger.info("[{}]-[checkForgettenQuestionIsRight method]-[用户 {} 正确回答忘记密码问题]", CONTROLLER, username);
		return response;
		
	}
	
	/**
	 * 通过回答忘记密码重新设置密码
	 * @param username
	 * @param passwordNew
	 * @param forgetToken
	 * @return
	 */
	@RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> resetPasswordViaAnswerForgetQue(String username, String passwordNew, String forgetToken){
		logger.info("[{}]-[resetPasswordViaAnswerForgetQue method]-[接受客户端请求]", CONTROLLER);
		ServerResponse<String> response = iUserService.resetPasswordViaAnswerForgetQue(username,  passwordNew, forgetToken);
		if (!response.isSuccess()) {
			logger.info("[{}]-[resetPasswordViaAnswerForgetQue method]-[用户 {} 重新设置密码失败,响应描述为：{}]", CONTROLLER, 
					username, response.getMsg());
			return response;
		}
		logger.info("[{}]-[resetPasswordViaAnswerForgetQue method]-[用户 {} 重新设置密码成功,响应描述为：{}]", CONTROLLER,
				username, response.getMsg());
		return response;
	}
	
	/**
	 * 在登陆状态下重新设置密码
	 * @param session
	 * @param passwordOld
	 * @param passwordNew
	 * @return
	 */
	@RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew){
		logger.info("[{}]-[resetPassword method]-[接受客户端请求]", CONTROLLER);
		ServerResponse<String> response;
		//先校验用户是否已经登陆
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			response = ServerResponse.createByErrorResReturnMsg("当前用户未登陆");
			logger.info("[{}]-[resetPassword method]-[用户 {} 重新设置密码失败,响应描述为：{}]", CONTROLLER, 
					user.getUsername(), response.getMsg());
			return response;
		}
		
		response = iUserService.resetPassword(user, passwordOld, passwordNew);
		if (!response.isSuccess()) {
			logger.info("[{}]-[resetPassword method]-[用户 {} 重新设置密码失败,响应描述为：{}]", CONTROLLER, 
					user.getUsername(), response.getMsg());
			return response;
		}
		logger.info("[{}]-[resetPassword method]-[用户 {} 重新设置密码成功,响应描述为：{}]", CONTROLLER,
				user.getUsername(), response.getMsg());
		return response;
		
	}
	
	/**
	 * 更新个人信息
	 * @param session
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "update_information.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> updatePersonalInformation(HttpSession session, User user){
		logger.info("[{}]-[updatePersonalInformation method]-[接受客户端请求]", CONTROLLER);
		ServerResponse<String> response;
		//先校验用户是否已经登陆
		User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
		if (currentUser == null) {
			response = ServerResponse.createByErrorResReturnMsg("当前用户未登陆");
			logger.info("[{}]-[updatePersonalInformation method]-[用户更新个人信息失败,响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		}
		
		user.setId(currentUser.getId());
		user.setUsername(currentUser.getUsername());
		response = iUserService.updatePersonalInformation(user);
		if (!response.isSuccess()) {
			logger.info("[{}]-[updatePersonalInformation method]-[用户 {} 更新个人信息失败,响应描述为：{}]", CONTROLLER, 
					user.getUsername(), response.getMsg());
			return response;
		}
		
		session.setAttribute(Const.CURRENT_USER, user);
		logger.info("[{}]-[updatePersonalInformation method]-[用户 {} 更新个人信息成功,响应描述为：{}]", CONTROLLER,
				user.getUsername(), response.getMsg());
		return response;
	}
	
	
	/**
	 * 获取个人信息强制登陆
	 * @param session
	 * @return
	 */
	@RequestMapping("get_information.do")
	@ResponseBody
	public <T> ServerResponse<T> getPersonalInformation(HttpSession session){
		logger.info("[{}]-[getPersonalInformation method]-[接受客户端请求]", CONTROLLER);
		ServerResponse response;
		//先校验用户是否已经登陆
		User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
		if (currentUser == null) {
			response = ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),"未登录,需要强制登录status=10");
			logger.info("[{}]-[getPersonalInformation method]-[获取个人信息强制登陆失败,响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		}
		
		currentUser.setPassword("");
		response = ServerResponse.createBySucessResReturnData(currentUser);
		logger.info("[{}]-[getPersonalInformation method]-[用户 {} 获取个人信息强制登陆成功,响应描述为：{}]", CONTROLLER,
				currentUser.getUsername(), response.getMsg());
		return response;
	}
	
	
	/**
	 * 发送验证码
	 * @param phone
	 */
	@RequestMapping("send_auth_code.do")
	@ResponseBody
	public ServerResponse<String> sendAuthCode(String username, String type){
		logger.info("[{}]-[sendAuthCode method]-[接受客户端请求]", CONTROLLER);
		ServerResponse<String> response = null;
		if (StringUtils.equals(type, Const.PHONE)) {
			response = iUserService.sendSMSAuthCode(username);
		} else if (StringUtils.equals(type, Const.EMAIL)) {
			response = iUserService.sendEmailAuthCode(username);
		}
		
		logger.info("[{}]-[sendAuthCode method]-[已发送短息验证码，响应描述为：{}]", CONTROLLER,
				 response.getMsg());
		return response;
	}
	
}
