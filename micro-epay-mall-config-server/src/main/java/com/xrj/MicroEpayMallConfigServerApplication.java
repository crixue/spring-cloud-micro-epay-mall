package com.xrj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class MicroEpayMallConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroEpayMallConfigServerApplication.class, args);
	}
}
