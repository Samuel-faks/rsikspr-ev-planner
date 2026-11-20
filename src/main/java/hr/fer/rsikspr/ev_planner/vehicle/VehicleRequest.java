package hr.fer.rsikspr.ev_planner.vehicle;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record VehicleRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @DecimalMin("10") @DecimalMax("200") Double batteryKwh,
        @NotNull @DecimalMin("5") @DecimalMax("50") Double consumptionKwhPer100km,
        @NotNull @DecimalMin("3") @DecimalMax("400") Double maxChargePowerKw) {
}
