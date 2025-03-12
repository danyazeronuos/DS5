package org.zero.dis1.repository;

import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import org.zero.dis1.entity.Reserved;
import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.utils.VertxDatabase;

import java.util.function.Consumer;

public class VertxReservedRepository {
    private final VertxDatabase db = VertxDatabase.getInstance();

    private SqlClient getClient(SqlConnection connection) {
        if (connection == null) {
            var foundedConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

            return foundedConnection.orElse(null);
        }

        return connection;
    }

    public void save(Reserved reserved, Consumer<Boolean> consumer, SqlConnection connection) {
        var request = "insert into reserved (user_id, trip_id) values ($1, $2)";

        getClient(connection).preparedQuery(request).execute(Tuple.of(reserved.getUserId(), reserved.getTripId()), ar -> {
            if (ar.succeeded()) {
                consumer.accept(true);
            } else {
                System.out.println("Error inserting reserved trip " + ar.cause().getMessage());
                consumer.accept(false);
            }
        });
    }
}
