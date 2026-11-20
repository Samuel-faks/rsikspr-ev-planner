package hr.fer.rsikspr.ev_planner.trip;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import hr.fer.rsikspr.ev_planner.error.BadRequestException;
import hr.fer.rsikspr.ev_planner.error.NotFoundException;
import hr.fer.rsikspr.ev_planner.error.UnreachableException;

@WebMvcTest(TripController.class)
class TripControllerTest {

    private static final String VALID = """
            {"vehicleId": 1, "fromStationId": "S0001", "toStationId": "S0177",
             "startSocPercent": 80, "minSocPercent": 10, "departureTime": "2026-11-15T08:00:00Z"}
            """;

    /** The example Trip from openapi.yaml. */
    private static final TripResponse TRIP = new TripResponse(1L, 1L, "S0001", "S0177",
            Instant.parse("2026-11-15T08:00:00Z"), 15.0, 492.5, 328.3, 23.9, 372.2, 88.7, 31.8,
            List.of(new TripStopResponse(1, "S0591", "Odmorište Banja Luka – Prijedor 1", 30.2, 80.0, 29.9, 11.9),
                    new TripStopResponse(2, "S0585", "Odmorište Mostar – Livno 3", 30.2, 80.0, 29.9, 12.0)));

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TripService tripService;

    private ResultActions postTrip(String body) throws Exception {
        return mvc.perform(post("/api/v1/trips").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void createReturns201WithLocationAndStops() throws Exception {
        when(tripService.create(any())).thenReturn(TRIP);

        postTrip(VALID)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/trips/1"))
                .andExpect(jsonPath("$.totalDistanceKm").value(492.5))
                .andExpect(jsonPath("$.departureTime").value("2026-11-15T08:00:00Z"))
                .andExpect(jsonPath("$.stops.length()").value(2))
                .andExpect(jsonPath("$.stops[0].order").value(1))
                .andExpect(jsonPath("$.stops[0].stationName").value("Odmorište Banja Luka – Prijedor 1"));
    }

    @Test
    void createWithOutOfRangeSocReturns400() throws Exception {
        String invalid = VALID.replace("\"startSocPercent\": 80", "\"startSocPercent\": 120")
                .replace("\"minSocPercent\": 10", "\"minSocPercent\": 60");

        postTrip(invalid)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", containsString("startSocPercent")))
                .andExpect(jsonPath("$.detail", containsString("minSocPercent")));

        verify(tripService, never()).create(any());
    }

    @Test
    void createWithMissingDepartureTimeReturns400() throws Exception {
        postTrip("{\"vehicleId\": 1, \"fromStationId\": \"S0001\", \"toStationId\": \"S0177\","
                + " \"startSocPercent\": 80, \"minSocPercent\": 10}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("departureTime")));
    }

    @Test
    void createWithSameStationsReturns400() throws Exception {
        when(tripService.create(any()))
                .thenThrow(new BadRequestException("fromStationId i toStationId moraju biti različiti."));

        postTrip(VALID.replace("S0177", "S0001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("fromStationId i toStationId moraju biti različiti."));
    }

    @Test
    void createWithUnknownVehicleReturns404() throws Exception {
        when(tripService.create(any())).thenThrow(new NotFoundException("Vozilo 1 ne postoji."));

        postTrip(VALID)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void createUnreachableReturns422() throws Exception {
        when(tripService.create(any())).thenThrow(new UnreachableException(
                "Odredište S0177 nije dostižno iz S0001 uz minimalno stanje baterije 50 %."));

        postTrip(VALID)
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.title").value("Unprocessable Content"));
    }

    @Test
    void getReturnsTrip() throws Exception {
        when(tripService.get(1L)).thenReturn(TRIP);

        mvc.perform(get("/api/v1/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.arrivalSocPercent").value(31.8));
    }

    @Test
    void getMissingReturns404() throws Exception {
        when(tripService.get(999L)).thenThrow(new NotFoundException("Putovanje 999 ne postoji."));

        mvc.perform(get("/api/v1/trips/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Putovanje 999 ne postoji."));
    }
}
