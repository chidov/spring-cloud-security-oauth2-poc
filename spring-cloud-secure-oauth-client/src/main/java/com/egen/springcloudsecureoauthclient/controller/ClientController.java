package com.egen.springcloudsecureoauthclient.controller;

import com.egen.springcloudsecureoauthclient.client.Food;
import com.egen.springcloudsecureoauthclient.client.FoodClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author cdov
 */
@RestController
public class ClientController {

    private final FoodClient foodClient;

    @Autowired
    public ClientController(FoodClient foodClient) {
        this.foodClient = foodClient;
    }

    @GetMapping("/foods")
    public List<Food> getFoods(){
        return foodClient.foodList();
    }
}
