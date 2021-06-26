/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.models;

import java.util.ArrayList;
import java.util.List;
// import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Java class that maps to Mongo collection.
@Data
@Document(collection = "restaurants")
@NoArgsConstructor
public class RestaurantEntity {

  @Id
  private String id;

  @NonNull
  private String restaurantId;

  @NonNull
  private String name;

  @NonNull
  private String city;

  @NonNull
  private String imageUrl;

  @NonNull
  private Double latitude;

  @NonNull
  private Double longitude;

  @NonNull
  private String opensAt;

  @NonNull
  private String closesAt;

  @NonNull
  private List<String> attributes = new ArrayList<>();

}

