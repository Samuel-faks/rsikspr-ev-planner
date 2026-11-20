package hr.fer.rsikspr.ev_planner.planner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import hr.fer.rsikspr.ev_planner.planner.TripPlanner.Car;
import hr.fer.rsikspr.ev_planner.planner.TripPlanner.Link;
import hr.fer.rsikspr.ev_planner.planner.TripPlanner.Node;
import hr.fer.rsikspr.ev_planner.planner.TripPlanner.Plan;
import hr.fer.rsikspr.ev_planner.planner.TripPlanner.Request;
import hr.fer.rsikspr.ev_planner.planner.TripPlanner.Stop;
import tools.jackson.databind.json.JsonMapper;

/** Table 7.3: route A -> E on test-graph.json, vehicle C = 50 kWh, k = 20 kWh/100 km, Pv = 100 kW. */
class TripPlannerTest {

    private static final double EPS = 1e-6;
    private static final Car CAR = new Car(50, 20, 100);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record JsonNode(String id, double powerKw) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record JsonEdge(String source, String target, double distanceKm) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record JsonGraph(List<JsonNode> nodes, List<JsonEdge> edges) {
    }

    private static List<Node> nodes;
    private static List<Link> links;
    private final TripPlanner planner = new TripPlanner();

    @BeforeAll
    static void loadGraph() throws Exception {
        try (InputStream in = TripPlannerTest.class.getResourceAsStream("/test-graph.json")) {
            JsonGraph graph = JsonMapper.builder().build().readValue(in, JsonGraph.class);
            nodes = graph.nodes().stream().map(n -> new Node(n.id(), n.powerKw())).toList();
            links = graph.edges().stream().map(e -> new Link(e.source(), e.target(), e.distanceKm())).toList();
        }
    }

    private Optional<Plan> plan(int startSoc, int minSoc, double temperature) {
        return planner.plan(nodes, links, CAR, new Request("A", "E", startSoc, minSoc, temperature));
    }

    @Test
    void example1FastestRouteGoesThroughB() {
        Plan p = plan(80, 10, 15).orElseThrow();

        // A -> D -> E is shorter (240 km) but D charges at only 22 kW, so A -> B -> E is faster
        assertThat(p.route()).containsExactly("A", "B", "E");
        assertThat(p.totalDistanceKm()).isCloseTo(270, within(EPS));
        assertThat(p.drivingMinutes()).isCloseTo(180, within(EPS));
        assertThat(p.chargingMinutes()).isCloseTo(28.8, within(EPS));
        assertThat(p.totalMinutes()).isCloseTo(218.8, within(EPS));
        assertThat(p.energyKwh()).isCloseTo(54, within(EPS));
        assertThat(p.arrivalSocPercent()).isCloseTo(20, within(EPS));

        assertThat(p.stops()).hasSize(1);
        Stop b = p.stops().getFirst();
        assertThat(b.order()).isEqualTo(1);
        assertThat(b.stationId()).isEqualTo("B");
        assertThat(b.arrivalSocPercent()).isCloseTo(32, within(EPS));
        assertThat(b.departureSocPercent()).isCloseTo(80, within(EPS));
        assertThat(b.chargedKwh()).isCloseTo(24, within(EPS));
    }

    @Test
    void example2UnreachableWithMinSoc30() {
        assertThat(plan(80, 30, 15)).isEmpty();
    }

    @Test
    void example3LowStartSocGoesThroughD() {
        Plan p = plan(50, 10, 15).orElseThrow();

        // A -> D needs exactly the 20 kWh that is available (boundary of F5)
        assertThat(p.route()).containsExactly("A", "D", "E");
        assertThat(p.totalDistanceKm()).isCloseTo(240, within(EPS));
        assertThat(p.drivingMinutes()).isCloseTo(160, within(EPS));
        // 35 kWh / 22 kW * 60 = 95.4545... min; the table shows it to two decimals
        assertThat(p.chargingMinutes()).isCloseTo(95.45, within(0.01));
        assertThat(p.totalMinutes()).isCloseTo(265.45, within(0.01));
        assertThat(p.energyKwh()).isCloseTo(48, within(EPS));
        assertThat(p.arrivalSocPercent()).isCloseTo(24, within(EPS));

        Stop d = p.stops().getFirst();
        assertThat(d.stationId()).isEqualTo("D");
        assertThat(d.arrivalSocPercent()).isCloseTo(10, within(EPS));
        assertThat(d.chargedKwh()).isCloseTo(35, within(EPS));
    }

    @Test
    void example4ColdWeatherUsesMoreEnergy() {
        Plan p = plan(80, 10, 5).orElseThrow();

        assertThat(p.route()).containsExactly("A", "B", "E");
        assertThat(p.totalDistanceKm()).isCloseTo(270, within(EPS));
        assertThat(p.drivingMinutes()).isCloseTo(180, within(EPS));
        assertThat(p.chargingMinutes()).isCloseTo(33.12, within(EPS));
        assertThat(p.totalMinutes()).isCloseTo(223.12, within(EPS));
        assertThat(p.energyKwh()).isCloseTo(62.1, within(EPS));
        assertThat(p.arrivalSocPercent()).isCloseTo(11, within(EPS));

        Stop b = p.stops().getFirst();
        assertThat(b.arrivalSocPercent()).isCloseTo(24.8, within(EPS));
        assertThat(b.chargedKwh()).isCloseTo(27.6, within(EPS));
    }

    @Test
    void example5UnreachableBelowZero() {
        assertThat(plan(80, 10, -5)).isEmpty();
    }

    @Test
    void unknownStationIsUnreachable() {
        assertThat(planner.plan(nodes, links, CAR, new Request("A", "X", 80, 10, 15))).isEmpty();
    }
}
