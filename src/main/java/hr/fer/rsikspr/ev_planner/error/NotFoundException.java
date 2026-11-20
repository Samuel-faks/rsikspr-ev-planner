package hr.fer.rsikspr.ev_planner.error;

/**
 * A vehicle, station or trip does not exist (HTTP 404).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
