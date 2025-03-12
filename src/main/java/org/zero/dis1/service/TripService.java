package org.zero.dis1.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.zero.dis1.entity.Reserved;
import org.zero.dis1.entity.Trip;
import org.zero.dis1.entity.User;
import org.zero.dis1.repository.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TripService {

    public Long reserveTripJdbc(Integer tripId, Integer userId, Boolean commit) {
        var tripRepository = new JdbcTripRepository();
        var userRepository = new JdbcUserRepository();
        var reservedRepository = new JdbcReservedRepository();

        var start = System.nanoTime();

        var trip = tripRepository.getTripById(tripId);

        if (trip.isEmpty()) return System.nanoTime() - start;

        if (trip.get().getSeatsAvailable() <= 0) return System.nanoTime() - start;

        var user = userRepository.findUserById(userId);

        if (user.isEmpty() || user.get().getBalance() < trip.get().getPrice()) return System.nanoTime() - start;

        user.get().setBalance(user.get().getBalance() - trip.get().getPrice());

        try {
            userRepository.updateUser(user.get());
        } catch (SQLException e) {
            System.out.println("Rollback after update user balance");
            tripRepository.rollback();
            userRepository.rollback();
            return System.nanoTime() - start;
        }

        try {
            tripRepository.decreaseTripAvailableSeatsById(tripId);
        } catch (SQLException e) {
            System.out.println("Rollback after decrease trip available seats");
            tripRepository.rollback();
            userRepository.rollback();
            return System.nanoTime() - start;
        }

        var entity = Reserved.builder()
                .userId(userId)
                .tripId(tripId)
                .build();

        try {
            reservedRepository.save(entity);
        } catch (SQLException e) {
            System.out.println("Rollback after save reserved entity -> " + e.getMessage());
            tripRepository.rollback();
            userRepository.rollback();
            return System.nanoTime() - start;
        }

        if (!commit) {
            tripRepository.rollback();
            userRepository.rollback();
            return System.nanoTime() - start;
        }

        System.out.println("Commit changes");
        tripRepository.commit();
        userRepository.commit();

        return System.nanoTime() - start;
    }

    public Future<Long> reserveTripVertx(Integer tripId, Integer userId, Boolean commit) {
        var tripRepository = new VertxTripRepository();
        var userRepository = new VertxUserRepository();
        var reservedRepository = new VertxReservedRepository();

        var start = System.nanoTime();
        Promise<Long> promise = Promise.promise();

        var connectionPool = new HashMap<String, SqlConnection>();
        var transactionPool = new HashMap<String, Transaction>();

        Consumer<Boolean> changes = bool -> {
            if (!bool || !commit) {
                System.out.println("Changes will be rolled back");
                transactionPool.get("trip").rollback();
                transactionPool.get("user").rollback();
                promise.complete(System.nanoTime() - start);
                return;
            }

            System.out.println("Changes will be committed");
            transactionPool.get("trip").commit().onSuccess(v -> System.out.println("Committed")).onFailure(e -> System.out.println("Error -> " + e.getMessage()));
            transactionPool.get("user").commit().onSuccess(v -> System.out.println("Committed")).onFailure(e -> System.out.println("Error -> " + e.getMessage()));
            promise.complete(System.nanoTime() - start);
        };

        Consumer<Boolean> saveReservation = bool -> {
            System.out.println("Save reservation");
            if (!bool) {
                transactionPool.get("trip").rollback();
                transactionPool.get("user").rollback();
                promise.complete(System.nanoTime() - start);
                return;
            }

            var entity = Reserved.builder()
                    .userId(userId)
                    .tripId(tripId)
                    .build();

            reservedRepository.save(entity, changes, connectionPool.get("trip"));
        };

        Consumer<Boolean> updateAvailableSeats = bool -> {
            System.out.println("update seats");
            if (!bool) {
                System.out.println("update seats - rollback");
                transactionPool.get("trip").rollback();
                transactionPool.get("user").rollback();
                promise.complete(System.nanoTime() - start);
                return;
            }

            tripRepository.decreaseTripAvailableSeatsById(tripId, saveReservation, connectionPool.get("trip"));
        };

        Consumer<Trip> checkSeats = trip -> {
            System.out.println("Check seats");
            if (trip.getSeatsAvailable() <= 0) {
                promise.complete(System.nanoTime() - start);

                return;
            }

            userRepository.findUserById(userId, user -> {
                System.out.println("Check balance");
                if (user.getBalance() < trip.getPrice()) {
                    promise.complete(System.nanoTime() - start);
                    return;
                }


                user.setBalance(user.getBalance() - trip.getPrice());
                userRepository.updateUser(user, updateAvailableSeats, connectionPool.get("user"));
            }, connectionPool.get("user"));
        };

        BiConsumer<SqlConnection, Transaction> getTrip = (con, tx) -> {
            connectionPool.put("user", con);
            transactionPool.put("user", tx);
            tx.completion(v -> System.out.println("User transaction completed -> " + v.succeeded() + "  -  " + v.cause()));
            tripRepository.getTripById(tripId, checkSeats, connectionPool.get("trip"));
        };

        BiConsumer<SqlConnection, Transaction> startUserTransaction = (con, tx) -> {
            connectionPool.put("trip", con);
            transactionPool.put("trip", tx);
            tx.completion(v -> System.out.println("Trip transaction completed -> " + v.succeeded() + "  -  " + v.cause()));
            userRepository.startTransaction(getTrip);
        };

        tripRepository.startTransaction(startUserTransaction);

        return promise.future();
    }
}
