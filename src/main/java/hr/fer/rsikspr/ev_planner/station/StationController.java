package hr.fer.rsikspr.ev_planner.station;

import hr.fer.rsikspr.ev_planner.error.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stations")
@Tag(name = "stations", description = "Punionice (samo čitanje)")
public class StationController {

    private final StationRepository stationRepository;

    public StationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }


    @GetMapping
    @Operation(summary = "Popis punionica s paginacijom", description = "Poredano po id. size je najviše 200.")
    @ApiResponse(responseCode = "200", description = "Jedna stranica punionica.")
    @ApiResponse(responseCode = "400", description = "Neispravan page ili size.",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public PagedModel<StationResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        var stations = stationRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
        return new PagedModel<>(stations.map(StationResponse::from));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dohvaća punionicu")
    @ApiResponse(responseCode = "200", description = "Punionica.")
    @ApiResponse(responseCode = "404", description = "Punionica ne postoji.",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public StationResponse get(@PathVariable String id) {
        return stationRepository.findById(id)
                .map(StationResponse::from)
                .orElseThrow(() -> new NotFoundException("Punionica " + id + " ne postoji."));
    }
}
