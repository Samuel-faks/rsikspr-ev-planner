package hr.fer.rsikspr.ev_planner.station;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** The station controller talks to the repository directly, so the repository is the mock. */
@WebMvcTest(StationController.class)
class StationControllerTest {

    private static final Station ZAGREB = new Station("S0001", "Zagreb 1", "Zagreb", 45.81, 15.98, 150);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private StationRepository stationRepository;

    @Test
    void listReturnsPageSortedById() throws Exception {
        when(stationRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ZAGREB), PageRequest.of(0, 1, Sort.by("id")), 654));

        mvc.perform(get("/api/v1/stations").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("S0001"))
                .andExpect(jsonPath("$.content[0].powerKw").value(150))
                .andExpect(jsonPath("$.page.size").value(1))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(654));

        verify(stationRepository).findAll(PageRequest.of(0, 1, Sort.by("id")));
    }

    @Test
    void listUsesDefaultsPage0Size20() throws Exception {
        when(stationRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mvc.perform(get("/api/v1/stations")).andExpect(status().isOk());

        verify(stationRepository).findAll(PageRequest.of(0, 20, Sort.by("id")));
    }

    @Test
    void sizeOver200Returns400() throws Exception {
        mvc.perform(get("/api/v1/stations").param("size", "201"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("size: must be less than or equal to 200"));

        verifyNoInteractions(stationRepository);
    }

    @Test
    void negativePageReturns400() throws Exception {
        mvc.perform(get("/api/v1/stations").param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReturnsStation() throws Exception {
        when(stationRepository.findById("S0001")).thenReturn(Optional.of(ZAGREB));

        mvc.perform(get("/api/v1/stations/S0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Zagreb 1"))
                .andExpect(jsonPath("$.city").value("Zagreb"));
    }

    @Test
    void getMissingReturns404() throws Exception {
        when(stationRepository.findById("S9999")).thenReturn(Optional.empty());

        mvc.perform(get("/api/v1/stations/S9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Punionica S9999 ne postoji."));
    }
}
