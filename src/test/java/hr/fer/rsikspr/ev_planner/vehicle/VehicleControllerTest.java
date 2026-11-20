package hr.fer.rsikspr.ev_planner.vehicle;

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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import hr.fer.rsikspr.ev_planner.error.NotFoundException;

/** Only the web layer: the real controller and error handler, the service is a Mockito mock (no database). */
@WebMvcTest(VehicleController.class)
class VehicleControllerTest {

    private static final String VALID = """
            {"name": "Kompaktni EV", "batteryKwh": 60, "consumptionKwhPer100km": 18, "maxChargePowerKw": 150}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private VehicleService vehicleService;

    @Test
    void createReturns201WithLocation() throws Exception {
        when(vehicleService.create(any())).thenReturn(new VehicleResponse(1L, "Kompaktni EV", 60, 18, 150));

        mvc.perform(post("/api/v1/vehicles").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/vehicles/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Kompaktni EV"))
                .andExpect(jsonPath("$.batteryKwh").value(60.0));
    }

    @Test
    void createWithInvalidFieldsReturns400ListingFields() throws Exception {
        String invalid = """
                {"name": "", "batteryKwh": 500, "consumptionKwhPer100km": 18, "maxChargePowerKw": 150}
                """;

        mvc.perform(post("/api/v1/vehicles").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail", containsString("batteryKwh")))
                .andExpect(jsonPath("$.detail", containsString("name")));

        verify(vehicleService, never()).create(any());
    }

    @Test
    void createWithMissingFieldReturns400() throws Exception {
        mvc.perform(post("/api/v1/vehicles").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"X\", \"batteryKwh\": 60, \"consumptionKwhPer100km\": 18}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("maxChargePowerKw")));
    }

    @Test
    void createWithBrokenJsonReturns400() throws Exception {
        mvc.perform(post("/api/v1/vehicles").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void getReturnsVehicle() throws Exception {
        when(vehicleService.get(1L)).thenReturn(new VehicleResponse(1L, "Kompaktni EV", 60, 18, 150));

        mvc.perform(get("/api/v1/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxChargePowerKw").value(150.0));
    }

    @Test
    void getMissingReturns404() throws Exception {
        when(vehicleService.get(42L)).thenThrow(new NotFoundException("Vozilo 42 ne postoji."));

        mvc.perform(get("/api/v1/vehicles/42"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Vozilo 42 ne postoji."));
    }
}
