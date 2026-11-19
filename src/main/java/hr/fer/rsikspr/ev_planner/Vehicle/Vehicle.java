package hr.fer.rsikspr.ev_planner.Vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
@Table(name="vehicle")
public class Vehicle {
    @Id
    @GeneratedValue tatedValue(strategy=GenerationType.IDENTITY)

    private long id;
    private String name;
    @Column(name="battery_kwh") private double batteryKwh;
    @Column(name="consumption_kwh_per_100km") private double consumptionKwhPer100km;
    @Column(name="max_charge_power_kwh") private double maxChargePowerKwh;

    protected Vehicle(){}

    public Vehicle(String name, double batteryKwh,double consumptionKwhPer100km, double maxChargePowerKwh){}

}
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {}