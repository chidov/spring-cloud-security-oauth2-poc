package com.egen.springcloudsecurecli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;

import java.util.Arrays;

@SpringBootApplication
public class SpringCloudSecureCliApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudSecureCliApplication.class, args);
    }

    @Override
    public void run(String... arg0) throws Exception {

        System.out.println("cron job started");

        ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
        resourceDetails.setClientAuthenticationScheme(AuthenticationScheme.header);
        resourceDetails.setAccessTokenUri("http://localhost:8082/auth-service/oauth/token");

        //must be a valid scope or get an error; if empty, get all scopes (default); better to ask for one
        resourceDetails.setScope(Arrays.asList("food_read"));

        //must be valid client id or get an error
        resourceDetails.setClientId("egen");
        resourceDetails.setClientSecret("egensecret");

        //diff user results in diff authorities/roles coming out; preauth on roles fails for adam, works for barry
        resourceDetails.setUsername("ops");
        resourceDetails.setPassword("pass2");

        OAuth2RestTemplate template = new OAuth2RestTemplate(resourceDetails);
        //could also get scopes: template.getAccessToken().getScope()
        String token =  template.getAccessToken().toString();//.getValue();

        System.out.println("Token: " + token);

        try {
            String s = template.getForObject("http://localhost:8081/service/foods", String.class);
            System.out.println("Result: " + s);

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }


}
