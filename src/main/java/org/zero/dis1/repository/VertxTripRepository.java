package org.zero.dis1.repository;

import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;
import lombok.SneakyThrows;
import org.zero.dis1.entity.Trip;
import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.utils.VertxDatabase;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VertxTripRepository {
    private final VertxDatabase db = VertxDatabase.getInstance();

    private SqlClient getClient(SqlConnection connection) {
        if (connection == null) {
            var foundedConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

            return foundedConnection.orElse(null);
        }

        return connection;
    }

    public VertxTripRepository() {
        db.newDatabaseConnection(
                DatabaseEnum.TRIP_DATABASE.get(),
                "postgres",
                "111"
        );
    }

    public void getTripById(Integer id, Consumer<Trip> consumer, SqlConnection connection) {
        var request = "select * from trip where id = $1";

        getClient(connection).preparedQuery(request)
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
    public void decreaseTripAvailableSeatsById(Integer id, Consumer<Boolean> consumer, SqlConnection connection) {
        var request = "UPDATE trip SET seats_available = seats_available - 1 WHERE id = $1 AND seats_available > 0";

        connection.preparedQuery(request).execute(Tuple.of(id), ar -> {
            if (ar.succeeded()) {
                System.out.println("Trip seats update success");
                consumer.accept(true);
                return;
            } else {
                System.out.println(ar.cause().getMessage());
                consumer.accept(false);
            }
        });


    }

    public void startTransaction(BiConsumer<SqlConnection, Transaction> consumer) {
        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            System.out.println("Trip connection not found");
            return;
        }

        tripConnection.get().getConnection().onSuccess(conn -> {
            conn.begin().onSuccess(tx -> consumer.accept(conn, tx));
        });
    }
}
