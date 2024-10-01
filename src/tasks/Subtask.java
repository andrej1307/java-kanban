
package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(Integer epicId,
                   String title,
                   String description,
                   LocalDateTime startTime,
                   Duration duration) {
        super(title, description, startTime, duration);
        this.epicId = epicId;
    }

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
        String result = "Subtask{id=" + getId() + ", " +
                "epic_id=" + epicId + ", " +
                "startTime='";
        if (getStartTime() == null) {
            result += "null'";
        } else {
            result += getStartTime().format(DATE_TIME_FORMATTER) + '\'';
        }
        result += ", duration='";
        if (getDuration() == null) {
            result += "null'";
        } else {
            result += getDuration().toHours() + ":" + getDuration().toMinutesPart() + '\'';
        }

        result += ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
        return result;
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
