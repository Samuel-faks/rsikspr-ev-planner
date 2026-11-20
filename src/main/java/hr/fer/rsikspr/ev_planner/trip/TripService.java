package hr.fer.rsikspr.ev_planner.trip;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hr.fer.rsikspr.ev_planner.error.BadRequestException;
import hr.fer.rsikspr.ev_planner.error.NotFoundException;
import hr.fer.rsikspr.ev_planner.error.UnreachableException;
import hr.fer.rsikspr.ev_planner.planner.TripPlanner;
import hr.fer.rsikspr.ev_planner.planner.TripPlanner.Plan;
import hr.fer.rsikspr.ev_planner.station.Station;
import hr.fer.rsikspr.ev_planner.station.StationLinkRepository;
import hr.fer.rsikspr.ev_planner.station.StationRepository;
import hr.fer.rsikspr.ev_planner.vehicle.Vehicle;
import hr.fer.rsikspr.ev_planner.vehicle.VehicleService;

/** Chapter 7.4: check the request, load the graph from the database, run the planner, save the result. */
@Service
public class TripService {

    /** Lab 1 always uses 15 °C; lab 2 will get it from the weather service. */
    static final double TEMPERATURE_C = 15;

    private final TripRepository tripRepository;
    private final VehicleService vehicleService;
    private final StationRepository stationRepository;
    private final StationLinkRepository stationLinkRepository;
    private final TripPlanner planner = new TripPlanner();

    public TripService(TripRepository tripRepository, VehicleService vehicleService,
            StationRepository stationRepository, StationLinkRepository stationLinkRepository) {
        this.tripRepository = tripRepository;
        this.vehicleService = vehicleService;
        this.stationRepository = stationRepository;
        this.stationLinkRepository = stationLinkRepository;
    }

    @Transactional
    public TripResponse create(TripRequest request) {
        // checks that involve two fields can't be done with annotations on one field -> 400 here
        if (request.fromStationId().equals(request.toStationId())) {
            throw new BadRequestException("fromStationId i toStationId moraju biti različiti.");
        }
        if (request.startSocPercent() <= request.minSocPercent()) {
            throw new BadRequestException("startSocPercent mora biti veći od minSocPercent.");
        }

        // 1. vehicle and both stations must exist (404)
        Vehicle vehicle = vehicleService.findEntity(request.vehicleId());
        requireStation(request.fromStationId());
        requireStation(request.toStationId());

        // 2. the whole graph from the database, converted to the planner's own records
        List<Station> stations = stationRepository.findAll();
        Map<String, String> names = stations.stream().collect(Collectors.toMap(Station::getId, Station::getName));
        List<TripPlanner.Node> nodes = stations.stream()
                .map(s -> new TripPlanner.Node(s.getId(), s.getPowerKw()))
                .toList();
        List<TripPlanner.Link> links = stationLinkRepository.findAll().stream()
                .map(l -> new TripPlanner.Link(l.getStationAId(), l.getStationBId(), l.getDistanceKm()))
                .toList();

        // 3.-5. graph, Dijkstra, result (422 if there is no route)
        var car = new TripPlanner.Car(vehicle.getBatteryKwh(), vehicle.getConsumptionKwhPer100km(),
                vehicle.getMaxChargePowerKw());
        var planRequest = new TripPlanner.Request(request.fromStationId(), request.toStationId(),
                request.startSocPercent(), request.minSocPercent(), TEMPERATURE_C);
        Plan plan = planner.plan(nodes, links, car, planRequest)
                .orElseThrow(() -> new UnreachableException("Odredište " + request.toStationId()
                        + " nije dostižno iz " + request.fromStationId()
                        + " uz minimalno stanje baterije " + request.minSocPercent() + " %."));

        // 6. save unrounded values; the stops are saved together with the trip (cascade)
        Trip trip = new Trip(vehicle.getId(), request.fromStationId(), request.toStationId(),
                request.departureTime().toInstant(), request.startSocPercent(), request.minSocPercent(),
                TEMPERATURE_C, plan.totalDistanceKm(), plan.drivingMinutes(), plan.chargingMinutes(),
                plan.totalMinutes(), plan.energyKwh(), plan.arrivalSocPercent());
        for (TripPlanner.Stop s : plan.stops()) {
            trip.addStop(new TripStop(s.order(), s.stationId(), s.arrivalSocPercent(), s.departureSocPercent(),
                    s.chargedKwh(), s.chargingMinutes()));
        }
        Trip saved = tripRepository.save(trip);
        return TripResponse.from(saved, names::get);
    }

    @Transactional(readOnly = true)
    public TripResponse get(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Putovanje " + id + " ne postoji."));
        // stops are LAZY, so they are read here, inside the transaction
        List<String> stopIds = trip.getStops().stream().map(TripStop::getStationId).toList();
        Map<String, String> names = stationRepository.findAllById(stopIds).stream()
                .collect(Collectors.toMap(Station::getId, Station::getName));
        return TripResponse.from(trip, names::get);
    }

    private void requireStation(String id) {
        if (!stationRepository.existsById(id)) {
            throw new NotFoundException("Punionica " + id + " ne postoji.");
        }
    }
}
