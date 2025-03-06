package org.zero.dis1.repository;

import lombok.SneakyThrows;
import org.zero.dis1.mapper.TripMapper;
import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.model.Trip;
import org.zero.dis1.utils.JdbcDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JdbcTripRepository implements org.zero.dis1.model.TripRepository {
    private final JdbcDatabase db = JdbcDatabase.getInstance();

    public JdbcTripRepository() {
        db.newDatabaseConnection(
                DatabaseEnum.TRIP_DATABASE.get(),
                "postgres",
                "111"
        );
    }

    @SneakyThrows
    public void getTrip(String request, Consumer<List<Trip>> consumer) {
        System.out.println("getAllTrip with JdbcDriver");
        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return;
        }

        var statement = tripConnection.get().createStatement();
        var response = statement.executeQuery(request);

        List<Trip> trips = new ArrayList<>();
        while (response.next()) {
            var trip = TripMapper.map(response);
            trips.add(trip);
        }


        consumer.accept(trips);
    }
}
