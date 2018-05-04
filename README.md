# Spring Cloud Security Oauth2 POC
This is a poc project for spring cloud security with oauth2 flow:
* OAuth 2.0 Grant Type: Authorization Code
* OAuth 2.0 Grant Type: Resource Owner Password Credentials

## OAuth 2.0 Grant Type: Authorization Code
This flow is which will allow you to grant part of authority by your consent when you type in your username and password by yourself.
project involve: spring-cloud-secure-ui and spring-cloud-secure-service

### Use Case
When you want to grant some basic authorization to a 3rd party web/app without providing them your credential or single sign on to other app using your social media account(ex. facebook).

### OAuth 2.0 Single Sign On
The default spring cloud security will be basic auth, Spring Cloud OAuth 2.0 Single Sign On will make your page authenticate via 3rd party login (in this case we use github) easily by just using @EnableOAuth2Sso to turn on signle sign on

```java
@Configuration
@EnableOAuth2Sso
public class SeurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/", "/login**")
        .permitAll()
            .anyRequest()
            .authenticated();
    }
}
```

we will need to setup the oauth client config

```
security:
  oauth2:
    client:
      clientId: aa974b4b9cb84c450615
      clientSecret: 06f34e736175fe24b4efca332b7574ffe24bb615
      accessTokenUri: https://github.com/login/oauth/access_token
      userAuthorizationUri: https://github.com/login/oauth/authorize
      clientAuthenticationScheme: form
    resource:
      userInfoUri: https://api.github.com/user
      preferTokenInfo: false
```

By having above configuration Spring Cloud OAuth 2.0 Single Sign On will process the Authorization Code flow for you automatically by getting the authorization code and request access token for you. You can get your access token by **OAuth2ClientContext**.

```java
@Controller
public class HomeController {
    @Autowired
    private final OAuth2ClientContext clientContext;
    
    @RequestMapping("/premier")
    public String displayPremier(){
        System.out.println("Token: " + clientContext.getAccessToken().getValue());
        return "premier";
    }
}
```

### Run it
```
http://localhost:8080/premier
```

## Creating a Resource Server and Routing Tokens to Services
We can build a resource server and let secure-ui call it by using the token get from github. 
### How Resource Server validate the token?
We can configure resource userInforUri which they will make a call to that endpoint to make sure the token is valid.

```
#for resource server token lookup
security.oauth2.resource.userInfoUri=https://api.github.com/user
#security.oauth2.resource.userInfoUri=http://localhost:8082/auth-service/user
```
#### CURL command for resource server endpoint
```bash
curl -X GET \
  http://localhost:8081/service/foods \
  -H 'Authorization: Bearer <access_token>' \
  -H 'Cache-Control: no-cache' 
```

### How UI call the service?
We can call the service by using **OAuth2RestTemplate**, here is how we config it in spring:
```java
@Configuration
public class ServiceConfig {

    @Bean
    public OAuth2RestTemplate oauth2RestTemplate(
            OAuth2ProtectedResourceDetails resource,
            OAuth2ClientContext context) {
        return new OAuth2RestTemplate(resource, context);
    }
```
We can use the OAuth2RestTemplate to call the resource server, which they will inject to token and do everything for you.
```java
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

    @RequestMapping("/foods")
    public String displayFood(Model model){
        System.out.println("Token: " + clientContext.getAccessToken().getValue());
        ResponseEntity<ArrayList<Food>> foods = oauth2RestTemplate.exchange(server.concat("/foods"), HttpMethod.GET, null, new ParameterizedTypeReference<ArrayList<Food>>(){});
        model.addAttribute("foods", foods.getBody());
        return "foods";
    }
}
```

## OAuth 2.0 Grant Type: Resource Owner Password Credentials
github doesn't support, so create our own auth server
spring-cloud-secure-auth-server and spring-cloud-secure-service

## Advance Token Option
JdbcTokenStore
JWT
