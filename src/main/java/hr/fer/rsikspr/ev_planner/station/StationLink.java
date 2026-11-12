package hr.fer.rsikspr.ev_planner.station;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Undirected road link between two stations (always stationAId < stationBId).
 * Station ids are plain strings, not @ManyToOne: route planning loads all ~58,000 links at once.
 */
@Entity
@Immutable
@Table(name = "station_link")
public class StationLink {

    @Id
    private Long id;

    @Column(name = "station_a_id")
    private String stationAId;

    @Column(name = "station_b_id")
    private String stationBId;

    @Column(name = "distance_km")
    private double distanceKm;

    protected StationLink() {
        // for JPA
    }

    public Long getId() {
        return id;
    }

    public String getStationAId() {
        return stationAId;
    }

    public String getStationBId() {
        return stationBId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }
}
