package x.funny.co.exception;

public class SwingUserInterfaceException extends RuntimeException {
    public SwingUserInterfaceException(String message) {
        super(message);
    }

    public SwingUserInterfaceException(Throwable cause) {
        super(cause);
    }

    public SwingUserInterfaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
