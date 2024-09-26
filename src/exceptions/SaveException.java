package exceptions;

public class SaveException extends RuntimeException {
    private final String filename;

    public SaveException(final String text, final String filename) {
        super(text);
        this.filename = filename;
    }

    public String getDetailMessage() {
        return getMessage() + " : " + filename;
    }

}
