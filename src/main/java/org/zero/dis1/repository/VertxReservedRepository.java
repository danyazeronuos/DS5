package org.zero.dis1.repository;

import io.vertx.sqlclient.Tuple;
import lombok.SneakyThrows;
import org.zero.dis1.entity.Reserved;
import org.zero.dis1.entity.Trip;
import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.model.ReservedRepository;
import org.zero.dis1.model.TripRepository;
import org.zero.dis1.utils.VertxDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VertxReservedRepository implements ReservedRepository {
    private final VertxDatabase db = VertxDatabase.getInstance();

    @Override
    public void save(Reserved reserved, Consumer<Boolean> consumer) {
        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            System.out.println("Trip connection not found");
            return;
        }

        var request = "insert into reserved (user_id, trip_id) values ($1, $2)";

        tripConnection.get().preparedQuery(request).execute(Tuple.of(reserved.getUserId(), reserved.getTripId()), ar -> {
            if (ar.succeeded()) {
                consumer.accept(true);
            } else {
                consumer.accept(false);
            }
        });
    }
}
