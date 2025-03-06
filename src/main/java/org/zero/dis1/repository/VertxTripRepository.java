package org.zero.dis1.repository;

import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.model.Trip;
import org.zero.dis1.model.TripRepository;
import org.zero.dis1.utils.VertxDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VertxTripRepository implements TripRepository {
    private final VertxDatabase db = VertxDatabase.getInstance();

    public VertxTripRepository() {
        db.newDatabaseConnection(
                DatabaseEnum.TRIP_DATABASE.get(),
                "postgres",
                "111"
        );
    }

    public void getTrip(String request, Consumer<List<Trip>> consumer) {
        System.out.println("getAllTrip with VertxDriver");
        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            System.out.println("Trip connection not found");
            return;
        }


        List<Trip> trips = new ArrayList<>();
        tripConnection.get().query(request).execute(ar -> {
            System.out.println(ar.succeeded());
            if (ar.succeeded()) {
                ar.result().forEach(row -> {
                    Trip trip = Trip.builder()
                            .id(row.getInteger("id"))
                            .destination(row.getString("destination"))
                            .price(row.getDouble("price"))
                            .seatsAvailable(row.getInteger("seats_available"))
                            .build();
                    trips.add(trip);
                });
                consumer.accept(trips);
            }
        });
    }
}
