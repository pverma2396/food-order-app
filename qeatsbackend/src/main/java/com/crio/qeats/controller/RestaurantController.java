/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.controller;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.services.RestaurantService;
import com.crio.qeats.utils.GeoLocation;

// import jdk.nashorn.internal.runtime.regexp.joni.Matcher;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
// import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;
// import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
// Implement Controller using Spring annotations.
// Remember, annotations have various "targets". They can be class level, method level or others.
@RestController
public class RestaurantController {

  public static final String RESTAURANT_API_ENDPOINT = "/qeats/v1";
  public static final String RESTAURANTS_API = "/restaurants";
  public static final String MENU_API = "/menu";
  public static final String CART_API = "/cart";
  public static final String CART_ITEM_API = "/cart/item";
  public static final String CART_CLEAR_API = "/cart/clear";
  public static final String POST_ORDER_API = "/order";
  public static final String GET_ORDERS_API = "/orders";

  @Autowired
  private RestaurantService restaurantService;



  //@GetMapping(RESTAURANTS_API)
  @RequestMapping(value = "/qeats/v1/restaurants", method = RequestMethod.GET)
  public ResponseEntity<GetRestaurantsResponse> getRestaurants(
      GetRestaurantsRequest getRestaurantsRequest) {

    try {
      GetRestaurantsResponse getRestaurantsResponse;

      GeoLocation geoLocation = new GeoLocation(getRestaurantsRequest.getLatitude(),
              getRestaurantsRequest.getLongitude());
        
      System.out.println(LocalTime.now());
      // Logger.info("Congrats! Your QEatsApplication server has started");

      if (!geoLocation.isValidGeoLocation()) {
        System.out.println(geoLocation.isValidGeoLocation());
        return (ResponseEntity.status(400).body(null));
      }

        
      //CHECKSTYLE:OFF
      getRestaurantsResponse = restaurantService
            .findAllRestaurantsCloseBy(getRestaurantsRequest, LocalTime.now());
      // log.info("getRestaurants returned {}", getRestaurantsResponse);
      //CHECKSTYLE:ON
      Pattern pattern = Pattern.compile(
                        "[^[a-z0-9 ]^[\\'\\!\\.\\,\\-\\/\\_\\&\\:\\@\\$\\#\\+\\)\\(]]",
                        Pattern.CASE_INSENSITIVE);
      List<Restaurant> restaurants = new ArrayList<Restaurant>();
      System.out.println(getRestaurantsResponse.getRestaurants().size());
      for (Restaurant grr : getRestaurantsResponse.getRestaurants()) {
        // System.out.println(grr.getName());
        Matcher m = pattern.matcher(grr.getName());
        boolean b = m.find();

        if (!b) {
          restaurants.add(grr);
        } else {
          int flag = 0;
          // System.out.println(grr.getName());
          if (grr.getName().contains("é")) {
            String correctedName = grr.getName().replace('é', 'e');
            grr.setName(correctedName);
            flag = 1;
          }
          if (grr.getName().contains("ö")) {
            String correctedName = grr.getName().replace('ö', 'o');
            grr.setName(correctedName);
            flag = 1;
          }
          if (grr.getName().contains("í")) {
            String correctedName = grr.getName().replace('í', 'i');
            grr.setName(correctedName);
            flag = 1;
          }
          if (grr.getName().contains("ñ")) {
            String correctedName = grr.getName().replace('ñ', 'n');
            grr.setName(correctedName);
            flag = 1;
          }
          if (grr.getName().contains("ñ")) {
            String correctedName = grr.getName().replace('ñ', 'n');
            grr.setName(correctedName);
            flag = 1;
          }
          if (grr.getName().contains("ó")) {
            String correctedName = grr.getName().replace('ó', 'o');
            grr.setName(correctedName);
            flag = 1;
          }
          if (grr.getName().contains("ä")) {
            String correctedName = grr.getName().replace('ä', 'a');
            grr.setName(correctedName);
            flag = 1;
          }
          if (grr.getName().contains("»")) {
            String correctedName = grr.getName().replace('»', '»');
            grr.setName(correctedName);
            flag = 1;
          }
          restaurants.add(grr);
          if (flag == 0) {
            System.out.println(grr.getName());
          }
        }
      }

      GetRestaurantsResponse getRestaurantsResponse2 = new GetRestaurantsResponse(restaurants);
      System.out.println(getRestaurantsResponse2.getRestaurants().size());
      // TimeUnit.SECONDS.sleep(3);
      System.out.println(LocalTime.now());
      return new ResponseEntity<>(getRestaurantsResponse2, HttpStatus.OK);
      // return ResponseEntity.status(HttpStatus.OK).body(getRestaurantsResponse);
        
      // return ResponseEntity.status(HttpStatus.OK).body(null);
    } catch (Exception ex) {
      return (ResponseEntity.status(400).body(null));
    }

  }

  // TIP(MODULE_MENUAPI): Model Implementation for getting menu given a restaurantId.
  // Get the Menu for the given restaurantId
  // API URI: /qeats/v1/menu?restaurantId=11
  // Method: GET
  // Query Params: restaurantId
  // Success Output:
  // 1). If restaurantId is present return Menu
  // 2). Otherwise respond with BadHttpRequest.
  //
  // HTTP Code: 200
  // {
  //  "menu": {
  //    "items": [
  //      {
  //        "attributes": [
  //          "South Indian"
  //        ],
  //        "id": "1",
  //        "imageUrl": "www.google.com",
  //        "itemId": "10",
  //        "name": "Idly",
  //        "price": 45
  //      }
  //    ],
  //    "restaurantId": "11"
  //  }
  // }
  // Error Response:
  // HTTP Code: 4xx, if client side error.
  //          : 5xx, if server side error.
  // Eg:
  // curl -X GET "http://localhost:8081/qeats/v1/menu?restaurantId=11"












}

