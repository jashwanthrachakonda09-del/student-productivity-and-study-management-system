package exceptions;

// UNIT III - Another user-defined exception.
// We throw this when the username or password is wrong.
public class LoginFailedException extends Exception {

    public LoginFailedException(String message) {
        super(message);
    }
}
