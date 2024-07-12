
/**
 * Класс описания подзадачи.
 */
package tasks;

public class Subtask extends Task  {
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

    @Override
    public String toString() {
        return "Subtask{id=" + getId() + ", " +
                "epic_id=" + epicId + ", " +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status='" + getStatus() +  '\'' +
                '}';
    }

    // Чтение идентификатора эпика подзадачи
    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        this.epicId = epicId;
    }
}
