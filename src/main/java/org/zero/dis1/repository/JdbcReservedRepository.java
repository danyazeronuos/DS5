package org.zero.dis1.repository;

import org.zero.dis1.entity.Reserved;
import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.model.ReservedRepository;
import org.zero.dis1.utils.JdbcDatabase;

import java.sql.SQLException;
import java.util.function.Consumer;

public class JdbcReservedRepository implements ReservedRepository {
    private final JdbcDatabase db = JdbcDatabase.getInstance();

    @Override
    public void save(Reserved reserved, Consumer<Boolean> consumer) {

        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return;
        }

        var request = "insert into reserved (user_id, trip_id) values (?, ?)";

        try (var statement = tripConnection.get().prepareStatement(request)) {
            statement.setInt(1, reserved.getUserId());
            statement.setInt(2, reserved.getTripId());
            statement.executeUpdate();
            consumer.accept(false);
        } catch (SQLException e) {
            consumer.accept(false);
        }
    }
}
