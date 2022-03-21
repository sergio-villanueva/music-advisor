package advisormvc;

// used for control flow when user attempts to access unauthorized commands
public class NotAuthorizedException extends Exception {
    public NotAuthorizedException() {
        this("Please, provide access for application.");

    }
    public NotAuthorizedException(String message) {
        super(message);
    }
}
