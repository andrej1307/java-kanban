package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Task {
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private String title;
    private String description;
    private int id;
    private TaskStatus status;
    private LocalDateTime startTime;
    private Duration duration;

    /**
     * Основной конструктор создания задачи
     *
     * @param title       - название задачи
     * @param description - описание задачи
     * @param startTime   - время начала выполнения задачи
     * @param duration    - продолжительность выполнения задачи
     */
    public Task(String title, String description, LocalDateTime startTime, Duration duration) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
        id = 0;
        status = TaskStatus.NEW;
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        id = 0;
        status = TaskStatus.NEW;
    }


    public Task(String title) {
        this.title = title;
        this.description = "-";
        id = 0;
        status = TaskStatus.NEW;
    }

    /**
     * конструктор копирования
     *
     * @param original - объект копирования
     */
    public Task(Task original) {
        title = original.getTitle();
        description = original.getDescription();
        startTime = original.getStartTime();
        duration = original.getDuration();
        id = original.getId();
        status = original.getStatus();
    }

    @Override
    public String toString() {
        String result = "Task{" +
                "id=" + id +
                ", startTime='";
        if (startTime == null) {
            result += "null'";
        } else {
            result += startTime.format(DATE_TIME_FORMATTER) + '\'';
        }
        result += ", duration='";
        if (duration == null) {
            result += "null'";
        } else {
            result += duration.toHours() + ":" + duration.toMinutesPart() + '\'';
        }
        result += ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        if (title != null) {
            hash = hash + title.hashCode();
        }
        hash = hash * 31;

        if (description != null) {
            hash = hash + description.hashCode();
        }
        return hash + id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // методчтения заголовка задачи
    // Во избежание путаниц заголовок задачи задается только в конструкторе
    public String getTitle() {
        return title;
    }

    // метод получения идентификатора задачи
    public int getId() {
        return id;
    }

    // метод присвоения идентификатора задачи
    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus newStatus) {
        this.status = newStatus;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    /**
     * расчет времени завершения задачи
     *
     * @return - время завершения
     */
    public LocalDateTime getEndTime() {
        if (startTime == null) {
            return null;
        } else {
            return startTime.plus(duration);
        }
    }

}
