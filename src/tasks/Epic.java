package tasks;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtasks;
    private LocalDateTime endTime;

    // конструктор с параметрами "имя", "описание"
    public Epic(String title, String description) {
        super(title, description);
        subtasks = new ArrayList<>();
    }

    // конструктор с одним параметром "имя"
    public Epic(String title) {
        super(title);
        subtasks = new ArrayList<>();
    }

    // Конструктор копирования объекта
    public Epic(Epic original) {
        super(original);
        subtasks = new ArrayList<>(original.getSubtasks());
    }

    // Переопределяем метод отображения объекта
    @Override
    public String toString() {
        String result = "Epic{" +
                "id=" + getId() +
                ", startTime='";

        if (getStartTime() == null) {
            result += "null'";
        } else {
            result += getStartTime().format(DATE_TIME_FORMATTER) + '\'';
        }
        result += ", duration='";
        if (endTime == null) {
            result += "null'";
        } else {
            result += getDuration().toHours() + ":" + getDuration().toMinutesPart() + '\'';
        }

        result += ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtasks=" + Arrays.toString(subtasks.toArray()) +
                '}';
        return result;
    }

    // получение списка идентификаторов подзадач
    public ArrayList<Integer> getSubtasks() {
        return new ArrayList<>(subtasks);
    }

    // метод добавления идентификатора подзадачи в список эпика
    public void addSubtask(Integer subtaskId) {
        int index = subtasks.indexOf(subtaskId);
        // Если идентификатор уже существует или равен идентификатору эпика
        // выходим без добавления задачи
        if ((index >= 0) || (subtaskId == getId())) return;

        subtasks.add(subtaskId);
    }

    // Удаление идентификатора подзадачи из списка эпика
    public void removeSubtask(Integer subtaskId) {
        int index = subtasks.indexOf(subtaskId);
        if (index < 0) {
            return;
        }
        subtasks.remove(index);
    }

    public void removeAllSubtasks() {
        subtasks.clear();
        setStatus(TaskStatus.NEW);
    }

    // метод переписывания списка подзадач, массивом новых идентификаторов
    public void reloadSubtakList(ArrayList<Integer> newsubtasks) {
        subtasks.clear();
        subtasks.addAll(newsubtasks);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

}
