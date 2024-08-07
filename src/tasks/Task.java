package tasks;

import java.util.Objects;

public class Task {
    private final String title;   // заголовок задачи не меняется в течении жизни
    private String description;
    private int id;
    private TaskStatus status;

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

    // конструктор копирования
    public Task(Task original) {
        title = original.getTitle();
        description = original.getDescription();
        id = original.getId();
        status = original.getStatus();
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(title, task.title) && Objects.equals(description, task.description);
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
}
