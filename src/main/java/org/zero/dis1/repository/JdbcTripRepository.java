package org.zero.dis1.repository;

import io.vertx.sqlclient.Tuple;
import lombok.SneakyThrows;
import org.zero.dis1.mapper.TripMapper;
import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.entity.Trip;
import org.zero.dis1.utils.JdbcDatabase;

import java.sql.SQLException;
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
    public void getTripById(Integer id, Consumer<Trip> consumer) {
        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return;
        }

        var request = "select * from trip t where t.id = ?";

        java.sql.ResultSet response;
        try (var statement = tripConnection.get().prepareStatement(request)) {
            statement.setInt(1, id);
            response = statement.executeQuery();
            if (response.next()) {
                consumer.accept(TripMapper.map(response));
                return;
            }
        }

        consumer.accept(null);
    }

    @SneakyThrows
    public void decreaseTripAvailableSeatsById(Integer id, Consumer<Boolean> consumer) {
        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return;
        }

        var request = "UPDATE trip SET seats_available = seats_available - 1 WHERE id = ? AND seats_available > 0";

        try (var statement = tripConnection.get().prepareStatement(request)) {
            statement.setInt(1, id);
            statement.executeUpdate();
            consumer.accept(true);
        } catch (SQLException e) {
            consumer.accept(false);
        }

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

    @Override
    public void startTransaction(Runnable runnable) {
        runnable.run();
    }

    @Override
    @SneakyThrows
    public void rollback() {
        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return;
        }

        tripConnection.get().rollback();
    }

    @Override
    @SneakyThrows
    public void commit() {
        var tripConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return;
        }

        tripConnection.get().commit();
    }

}
