package com.egen.springcloudsecureoauthclient.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @author cdov
 */
@FeignClient(name = "food",url="${food.ribbon.listOfServers}")
public interface FoodClient {

    @RequestMapping(method = RequestMethod.POST, value = "/foods", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Food> foodList();
}
