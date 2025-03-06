package org.zero.dis1.service;

import org.zero.dis1.model.TripRepository;

public class TripService {
    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public void reserveTrip(Integer tripId, Integer userId) {

    }
}
