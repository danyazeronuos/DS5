package org.zero.dis1.repository;

import org.zero.dis1.entity.Reserved;
import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.utils.JdbcDatabase;

import java.sql.SQLException;

public class JdbcReservedRepository {
    private final JdbcDatabase db = JdbcDatabase.getInstance();

    public void save(Reserved reserved) throws SQLException {

        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return;
        }

        var request = "insert into reserved (user_id, trip_id) values (?, ?)";

        var statement = tripConnection.get().prepareStatement(request);
        statement.setInt(1, reserved.getUserId());
        statement.setInt(2, reserved.getTripId());
        statement.executeUpdate();

//        throw new SQLException("Planned error.");

    }
}
