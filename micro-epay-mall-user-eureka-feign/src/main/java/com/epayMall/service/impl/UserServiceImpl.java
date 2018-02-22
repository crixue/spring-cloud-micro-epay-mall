package com.epayMall.service.impl;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epayMall.common.Const;
import com.epayMall.common.ServerResponse;
import com.epayMall.dao.UserMapper;
import com.epayMall.pojo.User;
import com.epayMall.redis.ICacheService;
import com.epayMall.service.IMailService;
import com.epayMall.service.IUserService;
import com.epayMall.util.MD5Util;
import com.epayMall.util.RandomGenerator;

/**
 * @author crixus
 *
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private ICacheService cacheService;
	@Autowired
	private IMailService iMailService;

	@Override
	public ServerResponse<User> checkUserLogin(String userName, String pwd) {
		//先检验用户是否存在
		int count = userMapper.checkIfExistsUser(userName);
		if (count == 0) {
			return ServerResponse.createByErrorResReturnMsg("用户不存在");
		}
		
		//再检验用户密码是否正确
		String password = MD5Util.MD5EncodeUtf8(pwd);
		User user = userMapper.selectOneIfUserPwdIsRight(userName, password);
		if (user == null) {
			return ServerResponse.createByErrorResReturnMsg("密码错误");
		}
		
		//都正确将密码置空,回传
		user.setPassword(null);
		return ServerResponse.createBySucessResReturnMsgAndData("登陆成功", user);
	}

	/* 
	 * 校验username或者email的类型为用户名是否都不存在
	 * 都不存在时status为0，即isSuccess
	 * @see com.epayMall.service.IUserService#checkValid(java.lang.String, java.lang.String)
	 */
	@Override
	public ServerResponse<String> checkValid(String str, String type) {
        if(org.apache.commons.lang3.StringUtils.isNotBlank(type)){
            //开始校验
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if(resultCount > 0 ){
                    return ServerResponse.createByErrorResReturnMsg("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0 ){
                    return ServerResponse.createByErrorResReturnMsg("email已存在");
                }
            }
        }else{
            return ServerResponse.createByErrorResReturnMsg("参数错误");
        }
        return ServerResponse.createBySucessResReturnMsg("校验成功");
	}

	@Override
	public ServerResponse<String> register(User user) {
		ServerResponse<String> validRes1 = checkValid(user.getUsername(), Const.USERNAME); 
		if (!validRes1.isSuccess()) {
			return validRes1;
		}
		ServerResponse<String> validRes2 = checkValid(user.getEmail(), Const.EMAIL); 
		if (!validRes2.isSuccess()) {
			return validRes2;
		}
		
		String md5pwd = MD5Util.MD5EncodeUtf8(user.getPassword());
		user.setPassword(md5pwd);
		user.setRole(Const.Role.ROLE_CUSTOMER);
		
		int count = userMapper.insert(user);
		if (count == 0) {
			return ServerResponse.createByErrorResReturnMsg("注册失败");
		}
		
		return ServerResponse.createBySucessResReturnMsg("注册成功");
	}
	

	@Override
	public ServerResponse<String> getForgettenQuestion(String userName) {
		ServerResponse<String> validRes1 = checkValid(userName, Const.USERNAME); 
		ServerResponse<String> validRes2 = checkValid(userName, Const.EMAIL); 
		if (!validRes1.isSuccess() && !validRes2.isSuccess()) {
			ServerResponse<String> userVaild = ServerResponse.createByErrorResReturnMsg("不存在该用户");
			return userVaild;
		}
		
		String question = userMapper.getForgettenQuestion(userName);
		ServerResponse<String> response ;
		if (StringUtils.isBlank(question)) {
			response = ServerResponse.createByErrorResReturnMsg("该用户未设置找回密码问题");
		} else {
			response = ServerResponse.createBySucessResReturnData(question);
		}
		
		return response;
	}

	@Override
	public ServerResponse<String> checkAnswerIsRight(String username, String question, String answer) {
		int count = userMapper.checkAnswerIsRight(username, question, answer);
		ServerResponse<String> response ;
		if(count == 0){
			response = ServerResponse.createByErrorResReturnMsg("问题答案错误");
			return response;
		}
		
		/*
		 * 为正确回答问题的用户创建一个时长为12小时的token，
		 * 这样做是为了在调用修改密码接口时要将用户的token传回来加以验证，
		 * 避免别人直接使用用户名和密码调用修改密码的接口，修改用户密码
		 */
		String forgetToken = UUID.randomUUID().toString();
    	cacheService.set(Const.TOKEN_PREFIX+username, forgetToken, 12L, TimeUnit.HOURS);
		response = ServerResponse.createBySucessResReturnData(forgetToken);
		return response;
	}

	@Override
	public ServerResponse<String> resetPasswordViaAnswerForgetQue(String username, String passwordNew,
			String forgetToken) {
		ServerResponse<String> response;
		
		if (StringUtils.isBlank(forgetToken)) {
			response = ServerResponse.createByErrorResReturnMsg("token已经失效");
			return response;
		}
		
		String vaildToken = cacheService.getNONValue(Const.TOKEN_PREFIX+username);
		if (!StringUtils.equals(forgetToken, vaildToken)) {
			response = ServerResponse.createByErrorResReturnMsg("token错误,请重新获取重置密码的token");
			return response;
		}
		
		String md5password = MD5Util.MD5EncodeUtf8(passwordNew);
		int count = userMapper.updateUserPassword(username, md5password);
		if (count == 0) {
			response = ServerResponse.createByErrorResReturnMsg("修改密码操作失效");
			return response;
		}
		
		response = ServerResponse.createBySucessResReturnMsg("修改密码成功");
		return response;
	}

	@Override
	public ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew) {
		ServerResponse<String> response;
		int pwdCount = userMapper.checkPasswordByUserId(user.getId(), MD5Util.MD5EncodeUtf8(passwordOld));
		if (pwdCount == 0) {
			response = ServerResponse.createByErrorResReturnMsg("旧密码输入错误");
			return response;
		}
		
		user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
		int saveCount = userMapper.updateByPrimaryKeySelective(user);
		if (saveCount == 0) {
			response = ServerResponse.createByErrorResReturnMsg("重置密码失败，请重新输入密码");
			return response;
		}
		
		response = ServerResponse.createBySucessResReturnMsg("修改密码成功");
		return response;
	}

	@Override
	public ServerResponse<String> updatePersonalInformation(User user) {
		ServerResponse<String> response;
		int rowCount = userMapper.updateByPrimaryKeySelective(user);
		if (rowCount == 0) {
			response = ServerResponse.createByErrorResReturnMsg("更新个人信息失败，请重新更新");
			return response;
		}
		
		response = ServerResponse.createBySucessResReturnMsg("更新个人信息成功");
		return response;
	}
	
    //backend

    /**
     * 校验是否是管理员
     * @param user
     * @return
     */
	@Override
    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySucessRes();
        }
        return ServerResponse.createByErrorRes();
    }
	
	@Override
	public ServerResponse<String> sendSMSAuthCode(String phone){
		//检验该电话号码是否被注册过了
		boolean isExists = checkValid(phone, Const.USERNAME).isSuccess();
		if (!isExists) {
			return ServerResponse.createByErrorResReturnMsg("该账户已经被注册了，直接登陆");
		}
		
		//生成随机6位数字的验证码
		String randomDigit = RandomGenerator.sixRandomDigit();
		
		//调用第三方接口发送短信给用户
		//为使用phone注册为username的用户将其验证码存储到redis中
		return null;
		
	}
	
	@Override
	public ServerResponse<String> registerBySendingEmailAuthCode(String authCode, User user){
		String storedCode = cacheService.getNONValue(user.getUsername());
		if (StringUtils.isBlank(storedCode)) {
			return ServerResponse.createByErrorResReturnMsg("未通过该邮箱注册或者邮箱注册码已经失效！");
		}
		
		if (!StringUtils.equals(storedCode, authCode)) {
			return ServerResponse.createByErrorResReturnMsg("验证码错误！");
		}
		
		return register(user);
	}
	
	@Override
	public ServerResponse<String> sendEmailAuthCode(String email) {
		//生成随机6位数字的验证码
		String randomDigit = RandomGenerator.sixRandomDigit();
		
		//调用接口发送6位数字
		String subject = "epay-mall 注册验证码";
		String content = String.format("您的注册码为: %s ,该验证码将于30分钟后失效", randomDigit);
		boolean isSent = iMailService.sendSimpleMail(email, subject, content);
		if (!isSent) {
			return ServerResponse.createByErrorResReturnMsg("未成功向邮箱发送邮件！");
		}
		
		//为使用email注册为username的用户将其验证码存储到redis中,默认保存30分钟
		cacheService.set(email, randomDigit, 30L, TimeUnit.MINUTES);
		logger.info("已为邮箱为 {} 的注册用户发送验证码", email);
		return ServerResponse.createBySucessRes();
		
	}

}
