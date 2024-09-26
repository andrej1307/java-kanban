package ManagerExceptions;

public class TaskCrossTimeException extends RuntimeException {
    private final String existsTasks;

    public TaskCrossTimeException(final String text, final String existsTasks) {
        super(text);
        this.existsTasks = existsTasks;
    }

    public String getDetailMessage() {
        return getMessage() + "\n" + existsTasks;
    }
}
