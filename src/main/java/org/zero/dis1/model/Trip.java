package org.zero.dis1.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Trip {
      private Integer id;
      private String destination;
      private Double price;
      private Integer seatsAvailable;
}
