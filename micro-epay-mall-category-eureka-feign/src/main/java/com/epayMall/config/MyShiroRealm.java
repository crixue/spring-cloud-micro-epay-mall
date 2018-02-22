package com.epayMall.config;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.User;
import com.epayMall.service.IDataClientZuulForUser;

/**
 * 进行权限信息的验证
 * Realm 本质上是一个特定的安全 DAO：它封装与数据源连接的细节，得到Shiro 所需的相关的数据
 * @author crixus
 *
 */
@Component
public class MyShiroRealm extends AuthorizingRealm{
	private static final Logger logger = LoggerFactory.getLogger(MyShiroRealm.class);
	
	@Autowired
	private IDataClientZuulForUser iDataClientZuulForUser;
	
	/*
	 * 授权（authorization）授权访问控制，用于对用户进行的操作授权，证明该用户是否允许进行当前操作，如访问某个链接，某个资源文件等。
	 * @see org.apache.shiro.realm.AuthorizingRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		logger.debug("授权配置--》doGetAuthorizationInfo()");
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		User user = (User) principals.getPrimaryPrincipal();
		authorizationInfo.addRole(user.getRole().toString());
		if(user.getRole() == 1) {  //admin
			authorizationInfo.addStringPermission("add");
			authorizationInfo.addStringPermission("query");
			authorizationInfo.addStringPermission("update");
			authorizationInfo.addStringPermission("delete");
		} else if(user.getRole() == 0) {  //common user
			authorizationInfo.addStringPermission("query");
		}
		return authorizationInfo;
	}

	/* 
	 * 认证（authentication）是用来验证用户身份
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		logger.debug("认证配置-[doGetAuthorizationInfo]-[start]");
		String username = (String) token.getPrincipal();
		
		ServerResponse<User> response = iDataClientZuulForUser.getUserByUsername(username);
		if(!response.isSuccess()) {
			logger.info("[doGetAuthenticationInfo]-未能正确获取用户信息");
			return null;
		}
		User user = response.getData();
		if(user == null) {
			logger.info("[doGetAuthenticationInfo]-不存在该用户／用户名不存在");
			return null;
		}
		
		SimpleAuthenticationInfo authorizationInfo = new SimpleAuthenticationInfo(
				user,  //用户名
				user.getPassword(),  //密码
				getName());  //realm name
		logger.debug("认证配置-[doGetAuthorizationInfo]-[end]");
		return authorizationInfo;
	}

	public AuthenticationInfo doGetAuthenticationInfoService(AuthenticationToken token) throws AuthenticationException {
		return doGetAuthenticationInfo(token);
	}
	
}
