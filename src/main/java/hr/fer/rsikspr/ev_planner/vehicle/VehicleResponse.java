package hr.fer.rsikspr.ev_planner.vehicle;

/**
 * Vehicle as returned by the API (schema Vehicle in openapi.yaml).
 */
public record VehicleResponse(
        Long id,
        String name,
        double batteryKwh,
        double consumptionKwhPer100km,
        double maxChargePowerKw) {

    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(v.getId(), v.getName(), v.getBatteryKwh(),
                v.getConsumptionKwhPer100km(), v.getMaxChargePowerKw());
    }
}
