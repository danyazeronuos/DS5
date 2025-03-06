package org.zero.dis1.repository;

import io.vertx.sqlclient.Tuple;
import lombok.SneakyThrows;
import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.entity.Trip;
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

    public void rollback() {
        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            System.out.println("Trip connection not found");
            return;
        }

        var request = "rollback";

        tripConnection.get().query(request).execute();
    }

    @Override
    public void commit() {
        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            System.out.println("Trip connection not found");
            return;
        }

        var request = "commit";

        tripConnection.get().query(request).execute();
    }

    @Override
    public void getTripById(Integer id, Consumer<Trip> consumer) {
        var request = "select * from trip where id = $1";

        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            System.out.println("Trip connection not found");
            return;
        }

        tripConnection.get().preparedQuery(request)
                .execute(Tuple.of(id), ar -> {
                    if (ar.succeeded() && ar.result().rowCount() > 0) {

                        var row = ar.result().iterator().next();
                        Trip trip = Trip.builder()
                                .id(row.getInteger("id"))
                                .destination(row.getString("destination"))
                                .price(row.getDouble("price"))
                                .seatsAvailable(row.getInteger("seats_available"))
                                .build();

                        consumer.accept(trip);
                    } else {
                        consumer.accept(null);
                    }
                });

    }

    @SneakyThrows
    public void decreaseTripAvailableSeatsById(Integer id, Consumer<Boolean> consumer) {
        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return;
        }

        var request = "UPDATE trip SET seats_available = seats_available - 1 WHERE id = $1 AND seats_available > 0";

        tripConnection.get().preparedQuery(request).execute(Tuple.of(id), ar -> {
            if (ar.succeeded()) {
                consumer.accept(true);
                return;
            } else {
                System.out.println(ar.cause().getMessage());
                consumer.accept(false);
            }
        });

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

    @Override
    public void startTransaction(Runnable runnable) {
        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            System.out.println("Trip connection not found");
            return;
        }

        tripConnection.get().query("start transaction").execute(ar -> {
            if (ar.succeeded()) {
                runnable.run();
            }
        });
    }
}
