package hr.fer.rsikspr.ev_planner.vehicle;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "vehicles", description = "Električna vozila")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }


    @PostMapping
    @Operation(summary = "Kreira vozilo")
    @ApiResponse(responseCode = "201", description = "Vozilo je kreirano.",
            headers = @Header(name = "Location", description = "URI vozila, npr. /api/v1/vehicles/1"))
    @ApiResponse(responseCode = "400", description = "Neispravan zahtjev (pogreška validacije).",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        VehicleResponse created = vehicleService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dohvaća vozilo")
    @ApiResponse(responseCode = "200", description = "Vozilo.")
    @ApiResponse(responseCode = "404", description = "Vozilo ne postoji.",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public VehicleResponse get(@PathVariable Long id) {
        return vehicleService.get(id);
    }
}
