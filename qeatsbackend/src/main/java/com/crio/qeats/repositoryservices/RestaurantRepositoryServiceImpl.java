/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;


@Service
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {



  @Autowired
  private RedisConfiguration redisConfiguration;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  @Autowired
  private RestaurantRepository restaurantRepository;

  ModelMapper modelMapper = new ModelMapper();

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }

  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
        Double longitude, LocalTime currentTime, Double servingRadiusInKms) {
    
    List<Restaurant> restaurants = null;

    if (redisConfiguration.isCacheAvailable()) {
      restaurants 
          = findAllRestaurantsCloseFromCache(latitude, longitude, currentTime, servingRadiusInKms);
    } else {
      restaurants
          = findAllRestaurantsCloseFromDB(latitude, longitude, currentTime, servingRadiusInKms);
    }

    return restaurants;
  }


  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.
  public List<Restaurant> findAllRestaurantsCloseFromDB(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

    List<Restaurant> restaurants = new ArrayList<Restaurant>();
    List<RestaurantEntity> restaurants2 = null;

    restaurants2 = restaurantRepository.findAll();

    for (RestaurantEntity re: restaurants2) {
      if (isRestaurantCloseByAndOpen(re, currentTime, latitude, longitude, servingRadiusInKms)) {
        Restaurant r = modelMapper.map(re, Restaurant.class);
        restaurants.add(r);
      }

      //checking if app is crashing consistently for large no. of restaurants
      // Restaurant r = modelMapper.map(re, Restaurant.class);
      // restaurants.add(r);
    }
    //CHECKSTYLE:OFF
    return restaurants;
  }    
  
  public List<Restaurant> findAllRestaurantsCloseFromCache(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

    List<Restaurant> restaurants = new ArrayList<Restaurant>();
    GeoLocation geoLocation = new GeoLocation(latitude, longitude);

    GeoHash geohash = GeoHash.withCharacterPrecision(
              geoLocation.getLatitude(), geoLocation.getLongitude(), 7);
    
    try (Jedis jedis = redisConfiguration.getJedisPool().getResource()) {
      String res = jedis.get(geohash.toBase32());

      if (res == null) {
        String tobeMappedinJedis = "";
        restaurants = findAllRestaurantsCloseFromDB(geoLocation.getLatitude(),
                 geoLocation.getLongitude(), currentTime, servingRadiusInKms);

        try {
          tobeMappedinJedis = new ObjectMapper().writeValueAsString(restaurants);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }

        jedis.setex(geohash.toBase32(),
             GlobalConstants.REDIS_ENTRY_EXPIRY_IN_SECONDS, tobeMappedinJedis); 
      } else {
        try {
          restaurants = new ObjectMapper().readValue(res, new TypeReference<List<Restaurant>>(){
          });
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    return restaurants;

  }

  //CHECKSTYLE:ON
  // public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
  //     Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

  //   List<Restaurant> restaurants = null;
  //   // TODO: CRIO_TASK_MODULE_REDIS
  //   // We want to use cache to speed things up. 
  // Write methods that perform the same functionality,
  //   // but using the cache if it is present and reachable.
  //   // Remember, you must ensure that if cache is not present, the queries are directed at the
  //   // database instead.


  //   //CHECKSTYLE:OFF
  //   //CHECKSTYLE:ON


  








  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objective:
  // 1. Check if a restaurant is nearby and open. If so, it is a candidate to be returned.
  // NOTE: How far exactly is "nearby"?

  /**
   * Utility method to check if a restaurant is within the serving radius at a given time.
   * @return boolean True if restaurant falls within serving radius and is open, false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude,
          restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
          < servingRadiusInKms;
    }

    return false;
  }



}

