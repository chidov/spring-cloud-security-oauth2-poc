package com.egen.springcloudsecureauthserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @author cdov
 */
@RestController
public class UserController {

    @GetMapping("/user")
    public Principal getUser(Principal principal){
        return principal;
    }
}
