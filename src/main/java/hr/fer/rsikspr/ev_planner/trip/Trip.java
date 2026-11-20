package hr.fer.rsikspr.ev_planner.trip;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Planned trip (table "trip"). Numbers are stored unrounded; rounding happens only in TripResponse.
 */
@Entity
@Table(name = "trip")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "from_station_id")
    private String fromStationId;

    @Column(name = "to_station_id")
    private String toStationId;

    @Column(name = "departure_time")
    private Instant departureTime;

    @Column(name = "start_soc_percent")
    private int startSocPercent;

    @Column(name = "min_soc_percent")
    private int minSocPercent;

    @Column(name = "temperature_c")
    private double temperatureC;

    @Column(name = "total_distance_km")
    private double totalDistanceKm;

    @Column(name = "driving_minutes")
    private double drivingMinutes;

    @Column(name = "charging_minutes")
    private double chargingMinutes;

    @Column(name = "total_minutes")
    private double totalMinutes;

    @Column(name = "energy_kwh")
    private double energyKwh;

    @Column(name = "arrival_soc_percent")
    private double arrivalSocPercent;

    /** Stops are saved and deleted together with the trip (cascade). */
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopOrder")
    private List<TripStop> stops = new ArrayList<>();

    protected Trip() {
        // for JPA
    }

    public Trip(Long vehicleId, String fromStationId, String toStationId, Instant departureTime,
                int startSocPercent, int minSocPercent, double temperatureC,
                double totalDistanceKm, double drivingMinutes, double chargingMinutes,
                double totalMinutes, double energyKwh, double arrivalSocPercent) {
        this.vehicleId = vehicleId;
        this.fromStationId = fromStationId;
        this.toStationId = toStationId;
        this.departureTime = departureTime;
        this.startSocPercent = startSocPercent;
        this.minSocPercent = minSocPercent;
        this.temperatureC = temperatureC;
        this.totalDistanceKm = totalDistanceKm;
        this.drivingMinutes = drivingMinutes;
        this.chargingMinutes = chargingMinutes;
        this.totalMinutes = totalMinutes;
        this.energyKwh = energyKwh;
        this.arrivalSocPercent = arrivalSocPercent;
    }

    /** Adds a stop and sets its back-reference, so trip_id gets filled in on save. */
    public void addStop(TripStop stop) {
        stops.add(stop);
        stop.setTrip(this);
    }

    public Long getId() {
        return id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getFromStationId() {
        return fromStationId;
    }

    public String getToStationId() {
        return toStationId;
    }

    public Instant getDepartureTime() {
        return departureTime;
    }

    public int getStartSocPercent() {
        return startSocPercent;
    }

    public int getMinSocPercent() {
        return minSocPercent;
    }

    public double getTemperatureC() {
        return temperatureC;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public double getDrivingMinutes() {
        return drivingMinutes;
    }

    public double getChargingMinutes() {
        return chargingMinutes;
    }

    public double getTotalMinutes() {
        return totalMinutes;
    }

    public double getEnergyKwh() {
        return energyKwh;
    }

    public double getArrivalSocPercent() {
        return arrivalSocPercent;
    }

    public List<TripStop> getStops() {
        return stops;
    }
}
