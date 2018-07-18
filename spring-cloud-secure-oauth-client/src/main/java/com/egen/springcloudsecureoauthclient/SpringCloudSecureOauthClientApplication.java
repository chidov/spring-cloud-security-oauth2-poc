package com.egen.springcloudsecureoauthclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
//@EnableOAuth2Client
@EnableFeignClients
public class SpringCloudSecureOauthClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudSecureOauthClientApplication.class, args);
    }
}
