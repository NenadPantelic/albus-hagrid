package rs.ac.kg.fin.albus.hagrid.exception;

public class HagridException extends RuntimeException {

    public HagridException() {
    }

    public HagridException(String message) {
        super(message);
    }

    public HagridException(String message, Throwable cause) {
        super(message, cause);
    }
}
