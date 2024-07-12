package tasks;

import java.util.ArrayList;

public class Epic  extends Task {
    private ArrayList<Integer> subtaskList;

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

    // Переопределяем метод отображения объекта
    @Override
    public String toString() {
        return  "Epic{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskNumber=" + subtaskList.size() +
                '}';
    }

    // получение списка идентификаторов подзадач
    public ArrayList<Integer> getSubtasks() {
        return subtaskList;
    }

    // метод добавления идентификатора подзадачи в список эпика
    public void addSubtask(Integer subtaskId) {
        int index = subtaskList.indexOf(subtaskId);
        if (index >= 0) return;
        subtaskList.add(subtaskId);
    }

    // Удаление идентификатора подзадачи из списка эпика
    public void removeSubtask(Subtask subtaskid) {
        int index = subtaskList.indexOf(subtaskid);
        if (index >= 0) {
            subtaskList.remove(index);
        }
    }

    public void  removeSubtask(Integer subtaskId) {
        int index = subtaskList.indexOf(subtaskId);
        if (index < 0) { return; }
        subtaskList.remove(index);
    }

    public void removeAllSubtasks() {
        subtaskList.clear();
    }

    // метод переписывания списка подзадач, массивом новых идентификаторов
    public void reloadSubtakList( ArrayList<Integer> newSubtaskList) {
        subtaskList.clear();
        subtaskList.addAll(newSubtaskList);
    }
}
