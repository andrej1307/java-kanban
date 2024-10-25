package exceptions;

public class NotFoundException extends RuntimeException {
    private final String taskInfo;

    public NotFoundException(final String text, final String taskInfo) {
        super(text);
        this.taskInfo = taskInfo;
    }

    public String getDetailMessage() {
        return getMessage() + " " + taskInfo;
    }

}
