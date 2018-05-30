package com.egen.springcloudsecureservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
//@EnableResourceServer
public class SpringCloudSecureServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudSecureServiceApplication.class, args);
    }
}
