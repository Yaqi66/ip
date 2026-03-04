package Pudding.UI;

/**
 * Represents an application-level error in Pudding (e.g. bad user input,
 * missing file, corrupted data).
 */
public class PuddingException extends Exception {
    /**
     * @param message human-readable description of the error
     */
    public PuddingException(String message) {
        super(message);
    }
}
