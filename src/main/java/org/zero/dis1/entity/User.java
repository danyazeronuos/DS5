package org.zero.dis1.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class User {
        private Integer id;
        private String username;
        private Double balance;
}
