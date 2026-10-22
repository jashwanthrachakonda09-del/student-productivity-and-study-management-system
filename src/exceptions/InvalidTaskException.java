package exceptions;

// UNIT III - Creating our own exception subclass.
// We throw this when the user enters wrong task details (empty title, wrong date, etc.).
public class InvalidTaskException extends Exception {

    public InvalidTaskException(String message) {
        super(message);   // pass the message to the Exception class
    }
}
