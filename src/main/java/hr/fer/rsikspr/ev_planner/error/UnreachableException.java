package hr.fer.rsikspr.ev_planner.error;

/** The planner found no route that keeps the battery above the minimum -> 422. */
public class UnreachableException extends RuntimeException {

    public UnreachableException(String message) {
        super(message);
    }
}
