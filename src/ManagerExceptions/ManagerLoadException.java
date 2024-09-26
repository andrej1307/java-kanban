package ManagerExceptions;

public class ManagerLoadException extends RuntimeException {
    private final String filename;

    public ManagerLoadException(final String text, final String filename) {
        super(text);
        this.filename = filename;
    }

    public String getDetailMessage() {
        return getMessage() + " : " + filename;
    }
}
