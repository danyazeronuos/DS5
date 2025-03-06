package org.zero.dis1.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class Trip {
      private Integer id;
      private String destination;
      private Double price;
      private Integer seatsAvailable;
}
