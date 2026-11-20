package hr.fer.rsikspr.ev_planner.error;

/** Request is well-formed but its values don't make sense together (e.g. minSoc >= startSoc) -> 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
