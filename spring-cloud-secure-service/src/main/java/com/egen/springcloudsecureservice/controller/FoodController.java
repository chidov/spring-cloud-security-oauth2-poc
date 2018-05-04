package com.egen.springcloudsecureservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cdov
 */
@RestController
public class FoodController {
    @RequestMapping("/foods")
    @PreAuthorize("#oauth2.hasScope('food_read') and hasAuthority('ROLE_OPERATOR')")
    public List<Food> getFoodData() {

        Food instance1 = new Food("001", "Rice", "White Rice");
        Food instance2 = new Food("002", "Fried Rice", "Delicious Rice");

        List<Food> foods = new ArrayList<>();
        foods.add(instance1);
        foods.add(instance2);

        return foods;
    }
}
