package hr.fer.rsikspr.ev_planner.trip;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "trip_stop")
public class TripStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")          // foreign key to trip.id
    private Trip trip;

    @Column(name = "stop_order")           // "order" is an SQL keyword
    private int stopOrder;

    @Column(name = "station_id")
    private String stationId;

    @Column(name = "arrival_soc_percent")
    private double arrivalSocPercent;

    @Column(name = "departure_soc_percent")
    private double departureSocPercent;

    @Column(name = "charged_kwh")
    private double chargedKwh;

    @Column(name = "charging_minutes")
    private double chargingMinutes;

    protected TripStop() {
        // for JPA
    }

    public TripStop(int stopOrder, String stationId, double arrivalSocPercent, double departureSocPercent,
                    double chargedKwh, double chargingMinutes) {
        this.stopOrder = stopOrder;
        this.stationId = stationId;
        this.arrivalSocPercent = arrivalSocPercent;
        this.departureSocPercent = departureSocPercent;
        this.chargedKwh = chargedKwh;
        this.chargingMinutes = chargingMinutes;
    }

    void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Long getId() {
        return id;
    }

    public Trip getTrip() {
        return trip;
    }

    public int getStopOrder() {
        return stopOrder;
    }

    public String getStationId() {
        return stationId;
    }

    public double getArrivalSocPercent() {
        return arrivalSocPercent;
    }

    public double getDepartureSocPercent() {
        return departureSocPercent;
    }

    public double getChargedKwh() {
        return chargedKwh;
    }

    public double getChargingMinutes() {
        return chargingMinutes;
    }
}
