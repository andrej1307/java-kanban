package exceptions;

public class TimeIntersectionException extends RuntimeException {
    private final String existsTasks;

    public TimeIntersectionException(final String text, final String existsTasks) {
        super(text);
        this.existsTasks = existsTasks;
    }

    public String getDetailMessage() {
        return getMessage() + "\n" + existsTasks;
    }
}
