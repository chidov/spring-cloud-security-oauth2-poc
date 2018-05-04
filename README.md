# Spring Cloud Security Oauth2 POC
This is a poc project for spring cloud security with oauth2 flow:
* OAuth 2.0 Grant Type: Authorization Code
* OAuth 2.0 Grant Type: Resource Owner Password Credentials

## OAuth 2.0 Grant Type: Authorization Code
This flow is which will allow you to grant part of authority by your consent when you type in your username and password by yourself.
project involve: spring-cloud-secure-ui and spring-cloud-secure-service

### Use Case
When you want to let 3rd party

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

## OAuth 2.0 Grant Type: Resource Owner Password Credentials
github doesn't support, so create our own auth server
spring-cloud-secure-auth-server and spring-cloud-secure-service

## Advance Token Option
JdbcTokenStore
JWT
