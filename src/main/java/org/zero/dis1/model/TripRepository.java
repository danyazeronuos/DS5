package org.zero.dis1.model;

import java.util.List;
import java.util.function.Consumer;

public interface TripRepository {
    void getTrip(String request, Consumer<List<Trip>> consumer);
}
