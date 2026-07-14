package in.advohq.exception;

/** Thrown when a request conflicts with existing state (e.g. username already taken). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
