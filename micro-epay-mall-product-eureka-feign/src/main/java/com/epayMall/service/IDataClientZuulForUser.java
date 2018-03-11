package com.epayMall.service;

import org.apache.shiro.session.Session;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.User;
import com.epayMall.service.impl.DataClientZuulFallBackForUser;

@FeignClient(name="micro-epay-mall-client-zuul", fallback=DataClientZuulFallBackForUser.class)
public interface IDataClientZuulForUser {

	@GetMapping("/user/epay-mall/user/remote/getUserByUsername")
	ServerResponse<User> getUserByUsername(String username) ;
	
	@RequestMapping(value="/user/epay-mall/user/get/userSession", method=RequestMethod.POST)
	ServerResponse<Session> getUserSession(Session session);
}
