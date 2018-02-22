package com.epayMall.service.impl;

import org.apache.shiro.session.Session;
import org.springframework.stereotype.Component;

import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.User;
import com.epayMall.service.IDataClientZuulForUser;

@Component
public class DataClientZuulFallBackForUser implements IDataClientZuulForUser {

	@Override
	public ServerResponse<User> getUserByUsername(String username) {
		return ServerResponse.createByErrorRes();
	}

	@Override
	public ServerResponse<Session> getUserSession(Session session) {
		return ServerResponse.createByErrorRes();
	}

}
