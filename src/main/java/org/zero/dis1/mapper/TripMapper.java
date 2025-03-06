package org.zero.dis1.mapper;

import org.zero.dis1.entity.Trip;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TripMapper {
    public static Trip map(ResultSet resultSet) throws SQLException {
        return Trip.builder()
                .id(resultSet.getInt("id"))
                .destination(resultSet.getString("destination"))
                .price(resultSet.getDouble("price"))
                .seatsAvailable(resultSet.getInt("seats_available"))
                .build();
    }
}
