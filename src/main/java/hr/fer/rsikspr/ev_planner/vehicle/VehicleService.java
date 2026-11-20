package hr.fer.rsikspr.ev_planner.vehicle;

import hr.fer.rsikspr.ev_planner.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        Vehicle vehicle = new Vehicle(
                request.name(),
                request.batteryKwh(),
                request.consumptionKwhPer100km(),
                request.maxChargePowerKw());
        return VehicleResponse.from(vehicleRepository.save(vehicle));   // INSERT, id is filled in
    }

    @Transactional(readOnly = true)
    public VehicleResponse get(Long id) {
        return VehicleResponse.from(findEntity(id));
    }

    /**
     * Also used by the trip service, which needs the entity itself for the route calculation.
     */
    @Transactional(readOnly = true)
    public Vehicle findEntity(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vozilo " + id + " ne postoji."));
    }
}
