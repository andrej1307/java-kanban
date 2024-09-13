public class ManagerSaveException extends RuntimeException {
    private final String filename;

    public ManagerSaveException(final String text, final String filename) {
        super(text);
        this.filename = filename;
    }

    public String getDetailMessage() {
        return getMessage() + " : " + filename;
    }

}
