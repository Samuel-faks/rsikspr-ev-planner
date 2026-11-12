package hr.fer.rsikspr.ev_planner.station;

import hr.fer.rsikspr.ev_planner.error.NotFoundException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationRepository stationRepository;

    public StationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    /**
     * Page and size are plain @RequestParams (not Pageable): the spec wants 400 for size > 200,
     * while Pageable would silently cap it. Spring MVC validates @Min/@Max here by itself.
     */
    @GetMapping
    public PagedModel<StationResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        var stations = stationRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
        return new PagedModel<>(stations.map(StationResponse::from));
    }

    @GetMapping("/{id}")
    public StationResponse get(@PathVariable String id) {
        return stationRepository.findById(id)
                .map(StationResponse::from)
                .orElseThrow(() -> new NotFoundException("Punionica " + id + " ne postoji."));
    }
}
