package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskList;
    private LocalDateTime endTime;

    // конструктор с параметрами "имя", "описание"
    public Epic(String title, String description) {
        super(title, description);
        subtaskList = new ArrayList<>();
    }

    // конструктор с одним параметром "имя"
    public Epic(String title) {
        super(title);
        subtaskList = new ArrayList<>();
    }

    // Конструктор копирования объекта
    public Epic(Epic original) {
        super(original);
        subtaskList = new ArrayList<>(original.getSubtasks());
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
                ", subtaskNumber=" + subtaskList.size() +
                '}';
        return result;
    }

    // получение списка идентификаторов подзадач
    public ArrayList<Integer> getSubtasks() {
        return new ArrayList<>(subtaskList);
    }

    // метод добавления идентификатора подзадачи в список эпика
    public void addSubtask(Integer subtaskId) {
        int index = subtaskList.indexOf(subtaskId);
        // Если идентификатор уже существует или равен идентификатору эпика
        // выходим без добавления задачи
        if ((index >= 0) || (subtaskId == getId())) return;

        subtaskList.add(subtaskId);
    }

    // Удаление идентификатора подзадачи из списка эпика
    public void removeSubtask(Integer subtaskId) {
        int index = subtaskList.indexOf(subtaskId);
        if (index < 0) {
            return;
        }
        subtaskList.remove(index);
    }

    public void removeAllSubtasks() {
        subtaskList.clear();
        setStatus(TaskStatus.NEW);
    }

    // метод переписывания списка подзадач, массивом новых идентификаторов
    public void reloadSubtakList(ArrayList<Integer> newSubtaskList) {
        subtaskList.clear();
        subtaskList.addAll(newSubtaskList);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public Duration getDuration() {
        if (getStartTime() == null || endTime == null) {
            return null;
        }
        return Duration.between(getStartTime(), endTime);
    }

}
