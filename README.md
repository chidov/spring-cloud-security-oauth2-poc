# Spring Cloud Security Oauth2 POC
This is a poc project for spring cloud security with oauth2 flow:
* OAuth 2.0 Grant Type: Authorization Code
* OAuth 2.0 Grant Type: Resource Owner Password Credentials

## OAuth 2.0 Grant Type: Authorization Code
This flow is which will allow you to grant part of authority by your consent when you type in your username and password by yourself.  
**project involve** : spring-cloud-secure-ui, spring-cloud-secure-service

### Use Case
When you want to grant some basic authorization to a 3rd party web/app without providing them your credential or single sign on to other app using your social media account(ex. facebook).

### OAuth 2.0 Single Sign On
The default spring cloud security will be basic auth, Spring Cloud OAuth 2.0 Single Sign On will make your page authenticate via 3rd party login (in this case we use github) easily by just using @EnableOAuth2Sso to turn on signle sign on.

**@EnableOAuth2Sso**: marks your service as an OAuth 2.0 Client. This means that it will be responsible for redirecting Resource Owner (end user) to the Authorization Server where the user has to enter their credentials. After it's done the user is redirected back to the Client with Authorization Code (don't confuse with Access Code). Then the Client takes the Authorization Code and exchanges it for an Access Token by calling Authorization Server. Only after that, the Client can make a call to a Resource Server with Access Token.

Within **@EnableOAuth2Sso** we can see it contains **@EnableOauth2Client** is where your service becomes OAuth 2.0 Client. It makes it possible to forward access token (after it has been exchanged for Authorization Code) to downstream services in case you are calling those services via OAuth2RestTemplate. Base on [spring document](https://docs.spring.io/spring-security/oauth/apidocs/org/springframework/security/oauth2/config/annotation/web/configuration/EnableOAuth2Client.html), this annotation is used when you want to use OAuth2RestTemplate within call to service that use athorization code flow.

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

```yaml
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
We can build a resource server (spring-cloud-secure-service) and let secure-ui call it by using the token get from github. 
### How Resource Server validate the token?
There are 2 ways we can do validate the token:
#### TokenServices 
We can configure token service by extends `ResourceServerConfigurerAdapter` which assign tokenServices to validate the token, we can use it to validate 3 type of tokenStore:
* `RemoteTokenServices` with `InMemoryTokenStore` : remote to [checkTokenEndpoint](https://docs.spring.io/spring-security/oauth/apidocs/org/springframework/security/oauth2/provider/endpoint/CheckTokenEndpoint.html) of spring cloud oauth2 (authentication server) to validate the token.
* `DefaultTokenServices` with `JwtTokenStore` : validate token via JWT verification.
* `DefaultTokenServices` with `JdbcTokenStore` : connect to database via jdbc datasource to validate token.

#### User Info Property
We can configure resource userInforUri which they will make a call to that endpoint to make sure the token is valid.

```properties
#for resource server token lookup
security.oauth2.resource.userInfoUri=https://api.github.com/user
#use this when you want to validate with auth server
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
This flow is a little bit straight forward and you need to be trust that 3rd party application as you need to provide your credential in order to get the access token. Github doesn't support this feature, so we will create our own auth server.  
**project involve** : spring-cloud-secure-auth-server and spring-cloud-secure-service

### How to create authorization server?
**@EnableAuthorizationServer** will enable your server become Authorization Server, and you can also register your client service with different clientDetail.

```java
@EnableAuthorizationServer
@Configuration
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthServerConfig(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("egen")
                .secret("{noop}egensecret")
                .authorizedGrantTypes("authorization_code","refresh_token","password")
                .scopes("food_read","food_write")
            .and()
                .withClient("oauthclient")
                .secret("{noop}oauthclient-secret")
                .authorizedGrantTypes("client_credentials", "refresh_token")
                .authorities("ROLE_USER", "ROLE_OPERATOR")
                .scopes("food_read");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.
                authenticationManager(authenticationManager);
    }

    @Override
    public void configure(final AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
    }
}

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("user").password("{noop}password").roles("USER").build());
        manager.createUser(User.withUsername("ops").password("{noop}password").roles("USER", "OPERATOR").build());
        return manager;
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
```

**Note**: above configuration all using in memory method to store clientDetail and userDetail. We can store clientDetail in database with [jdbc option](http://www.baeldung.com/spring-security-oauth-dynamic-client-registration) `clients.jdbc(DataSource dataSource)` and also userDetail by custom userDetailService by implement `UserDetailService` interface and return it in the Bean.

#### Get Token Call
Here's the call to get access_token by diffent grant type.
```bash
# password grant type
$ curl -X POST \
  http://localhost:8082/auth-service/oauth/token \
  -H 'Authorization: Basic ZWdlbjplZ2Vuc2VjcmV0' \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=egen&grant_type=password&username=user&password=password&scope=food_read'
  
# client_credential grant type
$ curl -X POST \
  http://localhost:8082/auth-service/oauth/token \
  -H 'Authorization: Basic b2F1dGhjbGllbnQ6b2F1dGhjbGllbnQtc2VjcmV0' \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=oauthclient&grant_type=client_credentials'
```

### Switch service to use custom auth server
Here is how to change service auth server from github to custom one, and we can use Resource Owner Password Credentials to retrieve token from auth server and access to the resource server. 
```properties
#for resource server token lookup
security.oauth2.resource.userInfoUri=http://localhost:8082/auth-service/user
```

### Method Access Rule
We can limit the method access rule by using `@PreAuthorize` which use `SpEL` to setup access rule, we will limit method access base on scope and role:
```java
@RequestMapping("/foods")
@PreAuthorize("#oauth2.hasScope('food_read') and hasAuthority('ROLE_OPERATOR')")
public List<Food> getFoodData() {
...
}
```
This means only token have scope `food_read` and role `ROLE_OPERATOR` can access this method. we can acheive that by `@EnableGlobalMethodSecurity(prePostEnabled = true)` and custom `ResourceServerTokenServices` which will retrieve scope and role from the userInfo endpoint. Here is the configuration:
```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends GlobalMethodSecurityConfiguration {

    private final ResourceServerProperties sso;

    @Autowired
    public SecurityConfig(ResourceServerProperties sso) {
        this.sso = sso;
    }
    @Bean
    public ResourceServerTokenServices myUserInfoTokenServices() {
        return new CustomUserInfoTokenServices(sso.getUserInfoUri(), sso.getClientId());
    }
    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return new OAuth2MethodSecurityExpressionHandler();
    }
}
```
### Switch back to github authentication
since I didn't specify scope github OAuth App, it didn't support the method access rule comment out these code in order to use github authentication for `spring-cloud-secure-ui` and `spring-cloud-secure-service`.

```java
    //@PreAuthorize("#oauth2.hasScope('food_read') and hasAuthority('ROLE_OPERATOR')")
    
    //@Bean
    //public ResourceServerTokenServices myUserInfoTokenServices() {
    //    return new CustomUserInfoTokenServices(sso.getUserInfoUri(), sso.getClientId());
    //}
```

## Communicate between resource server in microservice perspective
In order to access resource server by token, we will need to request access token from the auth server. However we can talk between one resource server to the other one by using `OAuth2RestTemplate` which I have mention above. Let me detail about it in microservice perspective.

We are going to use `spring-cloud-secure-service` and `spring-cloud-secure-oauth-client` as the example project:
* `spring-cloud-secure-service` is the resource server which require `food_read` scope and `ROLE_OPERATOR` role in order to access the resource.
* `spring-cloud-secure-oauth-client` is the resource server which just require a normal token authentication (no specific scope and role) in order to access the resource.

### Problem of Re-use access token from resource server call
As I mention above by using `OAuth2RestTemplate` Spring Clould OAuth will reuse the access token in `OAuthClientContext` for the rest client call. However if the scope of access token is not match with scope that required by the other server. for example `spring-cloud-secure-oauth-client` is authenticate with `ROLE_USER`, if we forward the access token to `spring-cloud-secure-service` in order to access `/foods`. We will get AccessDeniedException as it require role `ROLE_OPERATOR`.

### Solution : allow microservice talk to each other with another specific client registration
As you notice that in the authrozation server, I have different clients detail registration `egen` and `oauthclient` client_id.
* **egen** is the client_id that required you to do whether password or authorization code authentication, everyone outsider who want to access resource server need to request token with this client_id.
* **oauthclient** is the client_id specific for `spring-cloud-secure-oauth-client` that required only client_credentials to authenticate, which specific to the traffic within the call between microservice. it has the valid role in order to access to `spring-cloud-secure-service` and other microservice if needed in the future by adding more scope and role to it.

In conclusion, all calls from unknown user will require a strict authentication go through egen client_id and a specific microservice call will go throught their specific client_id which have appropriate scope and role. 

### How Spring OAuth handle that?
First, `OAuth2RestTemplate` will forward the acccess token to make call to other service. If they failed with some exceptions. ex.AccessDeniedException. It will try to request for a new access token by using the information from the `OAuth2ProtectedResourceDetails` and make call again.

We can specify the `OAuth2ProtectedResourceDetails` in application.yml:
```yaml
security:
  oauth2:
    client:
      clientId: oauthclient
      clientSecret: oauthclient-secret
      accessTokenUri: http://localhost:8082/auth-service/oauth/token
      grant-type: client_credentials
      scope: food_read
    resource:
      userInfoUri: http://localhost:8082/auth-service/user
```
so once the token from `egen` client_id failed, it will get a new one by using `oauthclient` client_id details. 

## Advance Token Option
In this POC, I use default inMemoryTokenStore for the token storage. We can using [JdbcTokenStore](https://github.com/Baeldung/spring-security-oauth/blob/master/oauth-authorization-server/src/main/java/org/baeldung/config/OAuth2AuthorizationServerConfig.java) and [JWTTokenStore](https://github.com/Baeldung/spring-security-oauth/blob/master/oauth-authorization-server/src/main/java/org/baeldung/config/OAuth2AuthorizationServerConfigJwt.java) and [spring-oauth-jwt](http://www.baeldung.com/spring-security-oauth-jwt), which JWT invovle some token encryption and signature that secure the token.


## Reference 
http://cloud.spring.io/spring-cloud-security/single/spring-cloud-security.html#_oauth2_single_sign_on  
https://spring.io/blog/2017/09/15/security-changes-in-spring-boot-2-0-m4
