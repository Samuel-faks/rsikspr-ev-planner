package hr.fer.rsikspr.ev_planner.trip;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;


public record TripRequest(
        @NotNull Long vehicleId,
        @NotBlank String fromStationId,
        @NotBlank String toStationId,
        @NotNull @Min(1) @Max(100) Integer startSocPercent,
        @NotNull @Min(0) @Max(50) Integer minSocPercent,
        @NotNull OffsetDateTime departureTime) {
}
