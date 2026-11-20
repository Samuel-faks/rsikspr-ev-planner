package hr.fer.rsikspr.ev_planner.trip;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/trips")
@Tag(name = "trips", description = "Planiranje putovanja")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    @Operation(summary = "Planira i sprema putovanje",
            description = "Izračuna najbržu rutu od polazne do odredišne punionice i spremi rezultat. "
                    + "fromStationId i toStationId moraju biti različiti, a startSocPercent veći od "
                    + "minSocPercent. Decimalni brojevi u odgovoru zaokruženi su na jednu decimalu.")
    @ApiResponse(responseCode = "201", description = "Putovanje je isplanirano i spremljeno.",
            headers = @Header(name = "Location", description = "URI putovanja, npr. /api/v1/trips/1"))
    @ApiResponse(responseCode = "400", description = "Neispravan zahtjev.",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Vozilo ili punionica ne postoji.",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Odredište nije dostižno s tim vozilom i stanjem baterije.",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<TripResponse> create(@Valid @RequestBody TripRequest request) {
        TripResponse created = tripService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dohvaća spremljeno putovanje")
    @ApiResponse(responseCode = "200", description = "Putovanje.")
    @ApiResponse(responseCode = "404", description = "Putovanje ne postoji.",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public TripResponse get(@PathVariable Long id) {
        return tripService.get(id);
    }
}
