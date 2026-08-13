/**
 * Represents an invalid command or command argument supplied to Kachow.
 */
public class KachowException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a chatbot-specific exception with guidance for correcting the input.
     *
     * @param message explanation shown to the user
     */
    public KachowException(String message) {
        super(message);
    }

    /**
     * Creates a chatbot-specific exception caused by a lower-level parsing failure.
     *
     * @param message explanation shown to the user
     * @param cause lower-level cause of the invalid input
     */
    public KachowException(String message, Throwable cause) {
        super(message, cause);
    }
}
