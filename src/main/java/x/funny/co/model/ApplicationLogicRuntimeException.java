package x.funny.co.model;

public class ApplicationLogicRuntimeException extends RuntimeException {
    public ApplicationLogicRuntimeException(String message) {
        super(message);
    }

    public ApplicationLogicRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationLogicRuntimeException(Throwable cause) {
        super(cause);
    }
}
