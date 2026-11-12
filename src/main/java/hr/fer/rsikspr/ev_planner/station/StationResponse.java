package hr.fer.rsikspr.ev_planner.station;

/**
 * Station as returned by the API (schema Station in openapi.yaml).
 */
public record StationResponse(String id, String name, String city, double lat, double lon, int powerKw) {

    public static StationResponse from(Station s) {
        return new StationResponse(s.getId(), s.getName(), s.getCity(), s.getLat(), s.getLon(), s.getPowerKw());
    }
}
