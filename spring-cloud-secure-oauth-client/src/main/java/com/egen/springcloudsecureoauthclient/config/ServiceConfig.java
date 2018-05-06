package com.egen.springcloudsecureoauthclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

/**
 * @author cdov
 */
@Configuration
public class ServiceConfig {

    @Bean
    public OAuth2RestTemplate oauth2RestTemplate(
            OAuth2ProtectedResourceDetails resource,
            OAuth2ClientContext oAuth2ClientContext) {
        return new OAuth2RestTemplate(resource, oAuth2ClientContext);
    }

}