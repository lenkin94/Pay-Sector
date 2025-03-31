package app.paysector.exception;

public class BillsFeignException extends RuntimeException {
    public BillsFeignException() {
    }

    public BillsFeignException(String message) {
        super(message);
    }
}
