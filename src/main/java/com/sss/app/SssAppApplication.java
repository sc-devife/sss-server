package com.sss.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@SpringBootApplication
public class SssAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(SssAppApplication.class, args);
	}

}
