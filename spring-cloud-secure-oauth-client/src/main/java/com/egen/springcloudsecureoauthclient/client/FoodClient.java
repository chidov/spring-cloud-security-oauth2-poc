package com.egen.springcloudsecureoauthclient.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cdov
 */
@Service
public class FoodClient {
    private final OAuth2RestTemplate oAuth2RestTemplate;

    @Value("${server.base-url:http://localhost:8081/service}")
    private String server;

    @Autowired
    public FoodClient(OAuth2RestTemplate oAuth2RestTemplate) {
        this.oAuth2RestTemplate = oAuth2RestTemplate;
    }

    public List<Food> foodList(){
        ResponseEntity<ArrayList<Food>> foods = oAuth2RestTemplate.exchange(server.concat("/foods"), HttpMethod.GET, null, new ParameterizedTypeReference<ArrayList<Food>>(){});
        return foods.getBody();
    }
}
