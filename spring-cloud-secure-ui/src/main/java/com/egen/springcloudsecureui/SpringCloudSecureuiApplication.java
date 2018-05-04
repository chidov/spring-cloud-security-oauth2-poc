package com.egen.springcloudsecureui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;

@SpringBootApplication
public class SpringCloudSecureuiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringCloudSecureuiApplication.class, args);
    }
}
