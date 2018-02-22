package com.epayMall.config;

import java.security.MessageDigest;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.springframework.stereotype.Component;

import com.epayMall.util.MD5Util;

@Component("customCredentialsMatcher")
public class CustomCredentialsMatcher extends SimpleCredentialsMatcher {

	@Override
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;   
		Object accountCredentials = this.getCredentials(info);
		String credentials = String.valueOf(usernamePasswordToken.getPassword());
		
		String inputCredentials = MD5Util.MD5EncodeUtf8(credentials);
		return equals(inputCredentials, accountCredentials);
	}
	
	private boolean equals(String inputCredentials, Object accountCredentials) {
		byte[] b1 = inputCredentials.getBytes();
		byte[] b2 = toBytes(accountCredentials);
		
		return MessageDigest.isEqual(b1, b2);
	}
	
}
