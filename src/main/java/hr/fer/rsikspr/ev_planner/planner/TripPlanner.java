package hr.fer.rsikspr.ev_planner.planner;

import static hr.fer.rsikspr.ev_planner.planner.Formulas.CHARGE_TARGET_PERCENT;
import static hr.fer.rsikspr.ev_planner.planner.Formulas.STOP_OVERHEAD_MINUTES;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

/**
 * Finds the fastest route (chapter 7.4). No Spring and no database: the service hands it plain records,
 * so the tests can feed it test-graph.json directly.
 */
public class TripPlanner {

    public record Node(String id, double powerKw) {
    }

    public record Link(String stationAId, String stationBId, double distanceKm) {
    }

    public record Car(double batteryKwh, double consumptionKwhPer100km, double maxChargePowerKw) {
    }

    public record Request(String fromStationId, String toStationId, int startSocPercent, int minSocPercent,
            double temperatureC) {
    }

    public record Stop(int order, String stationId, double arrivalSocPercent, double departureSocPercent,
            double chargedKwh, double chargingMinutes) {
    }

    public record Plan(List<String> route, double totalDistanceKm, double drivingMinutes, double chargingMinutes,
            double totalMinutes, double energyKwh, double arrivalSocPercent, List<Stop> stops) {
    }

    /** JGraphT edge that also remembers the road distance, so the result can be computed from the path. */
    static class LinkEdge extends DefaultWeightedEdge {
        final double distanceKm;

        LinkEdge(double distanceKm) {
            this.distanceKm = distanceKm;
        }
    }

    /** Returns the fastest plan, or empty when the destination is not reachable (the service turns that into 422). */
    public Optional<Plan> plan(List<Node> nodes, List<Link> links, Car car, Request req) {
        Map<String, Node> byId = nodes.stream().collect(Collectors.toMap(Node::id, Function.identity()));
        double factor = Formulas.temperatureFactor(req.temperatureC());

        var graph = new SimpleDirectedWeightedGraph<String, LinkEdge>(LinkEdge.class);
        nodes.forEach(n -> graph.addVertex(n.id()));

        // every link from the database is a road in both directions
        for (Link link : links) {
            addEdge(graph, byId, car, req, factor, link.stationAId(), link.stationBId(), link.distanceKm());
            addEdge(graph, byId, car, req, factor, link.stationBId(), link.stationAId(), link.distanceKm());
        }

        if (!graph.containsVertex(req.fromStationId()) || !graph.containsVertex(req.toStationId())) {
            return Optional.empty();
        }
        GraphPath<String, LinkEdge> path =
                new DijkstraShortestPath<>(graph).getPath(req.fromStationId(), req.toStationId());
        if (path == null) {
            return Optional.empty();
        }
        return Optional.of(buildPlan(path, byId, car, req, factor));
    }

    private void addEdge(SimpleDirectedWeightedGraph<String, LinkEdge> graph, Map<String, Node> byId, Car car,
            Request req, double factor, String a, String b, double distanceKm) {
        // edges out of the destination or into the start are never part of the fastest route
        if (a.equals(req.toStationId()) || b.equals(req.fromStationId())) {
            return;
        }
        Node nodeB = byId.get(b);
        if (nodeB == null || !byId.containsKey(a)) {
            return;
        }
        int socA = departureSoc(a, req);                                                      // F4
        double energy = Formulas.energyKwh(distanceKm, car.consumptionKwhPer100km(), factor); // F2
        if (!Formulas.isReachable(energy, socA, req.minSocPercent(), car.batteryKwh())) {     // F5
            return;
        }
        boolean toDestination = b.equals(req.toStationId());
        double drive = Formulas.drivingMinutes(distanceKm);                                   // F3
        double charge = toDestination ? 0 : Formulas.chargingMinutes(                         // F7
                Formulas.chargeKwh(socA, energy, car.batteryKwh()), car.maxChargePowerKw(), nodeB.powerKw());
        double weight = Formulas.edgeWeight(drive, charge, toDestination);                    // F8

        // if the data has the same pair twice, keep the faster one
        LinkEdge existing = graph.getEdge(a, b);
        if (existing != null) {
            if (graph.getEdgeWeight(existing) <= weight) {
                return;
            }
            graph.removeEdge(existing);
        }
        LinkEdge edge = new LinkEdge(distanceKm);
        graph.addEdge(a, b, edge);
        graph.setEdgeWeight(edge, weight);
    }

    private Plan buildPlan(GraphPath<String, LinkEdge> path, Map<String, Node> byId, Car car, Request req,
            double factor) {
        List<String> route = path.getVertexList();
        List<LinkEdge> edges = path.getEdgeList();
        List<Stop> stops = new ArrayList<>();
        double distance = 0;
        double energyTotal = 0;
        double driving = 0;
        double charging = 0;
        double arrivalSoc = 0;

        for (int i = 0; i < edges.size(); i++) {
            String a = route.get(i);
            String b = route.get(i + 1);
            double d = edges.get(i).distanceKm;
            int socA = departureSoc(a, req);
            double energy = Formulas.energyKwh(d, car.consumptionKwhPer100km(), factor);
            arrivalSoc = Formulas.arrivalSocPercent(socA, energy, car.batteryKwh());          // F6

            distance += d;
            energyTotal += energy;
            driving += Formulas.drivingMinutes(d);

            if (!b.equals(req.toStationId())) {                                               // every middle vertex is a stop
                double chargeKwh = Formulas.chargeKwh(socA, energy, car.batteryKwh());
                double chargeMin = Formulas.chargingMinutes(chargeKwh, car.maxChargePowerKw(), byId.get(b).powerKw());
                // above 80 % on arrival: no charging, leave with what we have
                double departure = Math.max(arrivalSoc, CHARGE_TARGET_PERCENT);
                stops.add(new Stop(stops.size() + 1, b, arrivalSoc, departure, chargeKwh, chargeMin));
                charging += chargeMin;
            }
        }
        double total = driving + charging + STOP_OVERHEAD_MINUTES * stops.size();
        return new Plan(route, distance, driving, charging, total, energyTotal, arrivalSoc, stops);
    }

    /** F4: start SoC at the first station, 80 % everywhere else. */
    private static int departureSoc(String stationId, Request req) {
        return stationId.equals(req.fromStationId()) ? req.startSocPercent() : CHARGE_TARGET_PERCENT;
    }
}
