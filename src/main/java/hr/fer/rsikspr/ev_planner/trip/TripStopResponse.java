package hr.fer.rsikspr.ev_planner.trip;

import static hr.fer.rsikspr.ev_planner.trip.TripResponse.round1;


public record TripStopResponse(
        int order,
        String stationId,
        String stationName,
        double arrivalSocPercent,
        double departureSocPercent,
        double chargedKwh,
        double chargingMinutes) {

    public static TripStopResponse from(TripStop s, String stationName) {
        return new TripStopResponse(
                s.getStopOrder(),
                s.getStationId(),
                stationName,
                round1(s.getArrivalSocPercent()),
                round1(s.getDepartureSocPercent()),
                round1(s.getChargedKwh()),
                round1(s.getChargingMinutes()));
    }
}
