package org.zero.dis1.model;

import org.zero.dis1.entity.Trip;

import java.util.function.Consumer;

public interface TripRepository {
    void getTripById(Integer id, Consumer<Trip> consumer);
    void decreaseTripAvailableSeatsById(Integer id, Consumer<Boolean> consumer);
    void startTransaction(Runnable runnable);
    void rollback();
    void commit();
}
