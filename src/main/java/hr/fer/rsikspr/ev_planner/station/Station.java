package hr.fer.rsikspr.ev_planner.station;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Charging station. Table and data come from Flyway migration V1__graph.sql; the app only reads them.
 */
@Entity
@Immutable
@Table(name = "station")
public class Station {

    @Id
    private String id;

    private String name;

    private String city;

    private double lat;

    private double lon;

    @Column(name = "power_kw")
    private int powerKw;

    protected Station() {
        // for JPA
    }

    public Station(String id, String name, String city, double lat, double lon, int powerKw) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.lat = lat;
        this.lon = lon;
        this.powerKw = powerKw;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getPowerKw() {
        return powerKw;
    }
}
