package hr.fer.rsikspr.ev_planner.planner;

/**
 * Formulas F1-F8 from the lab (chapter 7.2). Pure functions, no Spring, no database.
 * State of charge (SoC) is in percent (0-100), energy in kWh, time in minutes.
 */
public final class Formulas {

    public static final double AVERAGE_SPEED_KMH = 90;
    public static final int CHARGE_TARGET_PERCENT = 80;
    public static final double STOP_OVERHEAD_MINUTES = 10;

    private Formulas() {
    }

    /** F1: temperature factor. */
    public static double temperatureFactor(double temperatureC) {
        if (temperatureC >= 15) {
            return 1.00;
        }
        if (temperatureC >= 0) {
            return 1.15;
        }
        return 1.30;
    }

    /** F2: energy for a link, E = d / 100 * k * f(T). */
    public static double energyKwh(double distanceKm, double consumptionKwhPer100km, double temperatureFactor) {
        return distanceKm / 100 * consumptionKwhPer100km * temperatureFactor;
    }

    /** F3: driving time, tv = d / 90 * 60. */
    public static double drivingMinutes(double distanceKm) {
        return distanceKm / AVERAGE_SPEED_KMH * 60;
    }

    /**
     * F5: can the link be driven? E &lt;= (SA - Smin) / 100 * C.
     * The percent difference is taken first (integers), then divided, so edge cases come out exact.
     */
    public static boolean isReachable(double energyKwh, int departureSocPercent, int minSocPercent, double batteryKwh) {
        return energyKwh <= (departureSocPercent - minSocPercent) / 100.0 * batteryKwh;
    }

    /** F6: SoC on arrival, SB = SA - E / C * 100. */
    public static double arrivalSocPercent(int departureSocPercent, double energyKwh, double batteryKwh) {
        return departureSocPercent - energyKwh / batteryKwh * 100;
    }

    /**
     * F7: energy to charge back up to 80 %: max(0, 80/100*C - (SA/100*C - E)),
     * written as (80 - SA)/100*C + E so the percent difference is exact.
     */
    public static double chargeKwh(int departureSocPercent, double energyKwh, double batteryKwh) {
        return Math.max(0, (CHARGE_TARGET_PERCENT - departureSocPercent) / 100.0 * batteryKwh + energyKwh);
    }

    /** F7: charging time, tp = Edopuna / min(Pv, Ps) * 60. */
    public static double chargingMinutes(double chargeKwh, double vehicleMaxChargeKw, double stationPowerKw) {
        return chargeKwh / Math.min(vehicleMaxChargeKw, stationPowerKw) * 60;
    }

    /** F8: edge weight, tv if B is the destination, otherwise tv + tp(B) + 10. */
    public static double edgeWeight(double drivingMinutes, double chargingMinutes, boolean toDestination) {
        return toDestination ? drivingMinutes : drivingMinutes + chargingMinutes + STOP_OVERHEAD_MINUTES;
    }
}
