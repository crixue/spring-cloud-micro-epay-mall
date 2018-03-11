package com.epayMall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

import com.epayMall.filter.ErrorHandleFilter;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableZuulProxy
@EnableSpringHttpSession
public class MicroEpayMallClientZuulApplication {
	
	private final Logger logger = LoggerFactory.getLogger(MicroEpayMallClientZuulApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(MicroEpayMallClientZuulApplication.class, args);
	}
	
	@Bean
	public ErrorHandleFilter errorHandleFilter(){
		logger.info("[ErrorHandleFilter]-启动错误信息拦截配置");
		return new ErrorHandleFilter();
	}
}
