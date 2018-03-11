package com.epayMall;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.epayMall.dao")
@ServletComponentScan
@EnableRedisHttpSession(maxInactiveIntervalInSeconds=1800)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
