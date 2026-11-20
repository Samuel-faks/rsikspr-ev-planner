package hr.fer.rsikspr.ev_planner.vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Electric vehicle (table "vehicle" from V2__init.sql).
 */
@Entity
@Table(name = "vehicle")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // the database generates the id
    private Long id;                                      // Long, not long: null until saved

    private String name;

    @Column(name = "battery_kwh")
    private double batteryKwh;

    @Column(name = "consumption_kwh_per100km")
    private double consumptionKwhPer100km;

    @Column(name = "max_charge_power_kw")
    private double maxChargePowerKw;

    protected Vehicle() {
        // for JPA
    }

    public Vehicle(String name, double batteryKwh, double consumptionKwhPer100km, double maxChargePowerKw) {
        this.name = name;
        this.batteryKwh = batteryKwh;
        this.consumptionKwhPer100km = consumptionKwhPer100km;
        this.maxChargePowerKw = maxChargePowerKw;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBatteryKwh() {
        return batteryKwh;
    }

    public double getConsumptionKwhPer100km() {
        return consumptionKwhPer100km;
    }

    public double getMaxChargePowerKw() {
        return maxChargePowerKw;
    }
}
