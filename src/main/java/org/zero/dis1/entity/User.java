package org.zero.dis1.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
        private Integer id;
        private String username;
        private Double balance;
}
