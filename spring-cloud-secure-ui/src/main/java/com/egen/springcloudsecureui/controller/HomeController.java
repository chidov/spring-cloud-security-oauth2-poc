package com.egen.springcloudsecureui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;

/**
 * @author cdov
 */
@Controller
public class HomeController {
    private final OAuth2ClientContext clientContext;

    private final OAuth2RestTemplate oauth2RestTemplate;

    @Value("${server.base-url:http://localhost:8081/service}")
    private String server;

    @Autowired
    public HomeController(OAuth2ClientContext clientContext, OAuth2RestTemplate oauth2RestTemplate) {
        this.clientContext = clientContext;
        this.oauth2RestTemplate = oauth2RestTemplate;
    }

    @RequestMapping("/")
    public String displayHome(){
        return "home";
    }

    @RequestMapping("/premier")
    public String displayPremier(){
        return "premier";
    }

    @RequestMapping("/foods")
    public String displayFood(Model model){
        System.out.println("Token: " + clientContext.getAccessToken().getValue());
        ResponseEntity<ArrayList<Food>> foods = oauth2RestTemplate.exchange(server.concat("/foods"), HttpMethod.GET, null, new ParameterizedTypeReference<ArrayList<Food>>(){});
        model.addAttribute("foods", foods.getBody());
        return "foods";
    }
}
