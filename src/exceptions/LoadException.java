package exceptions;

public class LoadException extends RuntimeException {
    private final String filename;

    public LoadException(final String text, final String filename) {
        super(text);
        this.filename = filename;
    }

    public String getDetailMessage() {
        return getMessage() + " : " + filename;
    }
}
