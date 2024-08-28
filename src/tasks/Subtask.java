
package tasks;

public class Subtask extends Task {
    private Integer epicId;

    // При вызове конструктора указание объекта задачи "эпик" обязательно
    public Subtask(Integer epicId, String title, String description) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(Integer epicId, String title) {
        super(title);
        this.epicId = epicId;
    }

    public Subtask(Subtask original) {
        super(original);
        this.epicId = original.getEpicId();
    }

    @Override
    public String toString() {
        return "Subtask{id=" + getId() + ", " +
                "epic_id=" + epicId + ", " +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }

    // Чтение идентификатора эпика подзадачи
    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        if (epicId == getId()) {
            return;
        }
        this.epicId = epicId;
    }
}
