package hr.fer.rsikspr.ev_planner.trip;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;


public record TripResponse(
        Long id,
        Long vehicleId,
        String fromStationId,
        String toStationId,
        Instant departureTime,
        double temperatureC,
        double totalDistanceKm,
        double drivingMinutes,
        double chargingMinutes,
        double totalMinutes,
        double energyKwh,
        double arrivalSocPercent,
        List<TripStopResponse> stops) {


    public static TripResponse from(Trip t, Function<String, String> stationNames) {
        List<TripStopResponse> stops = t.getStops().stream()
                .map(s -> TripStopResponse.from(s, stationNames.apply(s.getStationId())))
                .toList();
        return new TripResponse(
                t.getId(),
                t.getVehicleId(),
                t.getFromStationId(),
                t.getToStationId(),
                t.getDepartureTime(),
                round1(t.getTemperatureC()),
                round1(t.getTotalDistanceKm()),
                round1(t.getDrivingMinutes()),
                round1(t.getChargingMinutes()),
                round1(t.getTotalMinutes()),
                round1(t.getEnergyKwh()),
                round1(t.getArrivalSocPercent()),
                stops);
    }

    /**
     * One decimal, half up: 95.45 -> 95.5. BigDecimal.valueOf goes through the decimal text of the double,
     * so 95.45 is really treated as 95.45 (Math.round(x * 10) / 10.0 can get such cases wrong).
     */
    static double round1(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
