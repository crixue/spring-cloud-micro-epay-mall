package com.epayMall.service;

import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.User;

public interface IUserService {
	/**
	 * 验证用户登陆的合法性
	 * @param userName
	 * @param pwd
	 * @return
	 */
	ServerResponse<User> checkUserLogin(String userName, String pwd);
	
	/**
	 * 检验username或者email是否有被注册过
	 * @param str
	 * @param type
	 * @return
	 */
	ServerResponse<String> checkValid(String str,String type);
	
	/**
	 * 用户注册
	 * @param user
	 * @return
	 */
	ServerResponse<String> register(User user);
	
	/**
	 * 获取用户的忘记密码的问题
	 * @param userName
	 * @return
	 */
	ServerResponse getForgettenQuestion(String userName);
	
	/**
	 * 检验用户用户回答忘记密码问题是否正确
	 * @param username
	 * @param question
	 * @param answer
	 * @return
	 */
	ServerResponse<String> checkAnswerIsRight(String username, String question, String answer);

	/**
	 * 通过回答忘记密码重新设置密码
	 * @param username
	 * @param passwordNew
	 * @param forgetToken
	 * @return
	 */
	ServerResponse<String> resetPasswordViaAnswerForgetQue(String username, String passwordNew, String forgetToken);
	
	/**
	 * 在登陆状态下重新设置密码
	 * @param user
	 * @param passwordOld
	 * @param passwordNew
	 * @return
	 */
	ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew);
	
	/**
	 * 更新个人信息
	 * @param user
	 * @return
	 */
	ServerResponse<String> updatePersonalInformation(User user);

	ServerResponse checkAdminRole(User user);

	/**
	 * 发送短信验证码
	 * @param phone
	 * @return
	 */
	<T> ServerResponse<T> sendSMSAuthCode(String phone);
	
	/**
	 * 发送邮箱验证码
	 * @param email
	 * @return
	 */
	ServerResponse<String> sendEmailAuthCode(String email);

	
	/**
	 * 通过发送邮件验证码注册
	 * @param authCode
	 * @param user
	 * @return
	 */
	ServerResponse<String> registerBySendingEmailAuthCode(String authCode, User user);
	
}
