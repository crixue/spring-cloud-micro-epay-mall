package com.xrj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class MicroEpayMallEurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroEpayMallEurekaServerApplication.class, args);
	}
}
