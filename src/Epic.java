import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtaskList;

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
        return "Epic{" +
                "ID=" + getID() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskNumber=" + subtaskList.size() +
                '}';
    }

    // получение списка подзадач
    public ArrayList<Subtask> getSubtasks() {
        return subtaskList;
    }

    // метод добавления подзадачи в список эпика
    public void addSubtask(Subtask subtask) {
        subtaskList.add(subtask);
    }

    // метод вычисления статуса эпика
    public void calculateStatus() {
        if (subtaskList.isEmpty()) {
            return; // список подзадач пуст
        } else {
            TaskStatus newStatus = getStatus();
            int countTaskNew=0;
            int countTaskInProgress=0;
            int countTaskDone=0;

            for (Subtask s : subtaskList) {
                TaskStatus subtaskStatus = s.getStatus();
                if (subtaskStatus == TaskStatus.NEW) {
                    countTaskNew++;
                } else if (subtaskStatus == TaskStatus.IN_PROGRESS) {
                    countTaskInProgress++;
                } else if (subtaskStatus == TaskStatus.DONE) {
                    countTaskDone++;
                }
            }
            if (countTaskDone == subtaskList.size()) {
                newStatus = TaskStatus.DONE;
            } else if (countTaskDone > 0 || countTaskInProgress > 0) {
                newStatus = TaskStatus.IN_PROGRESS;
            } else if(countTaskDone == 0 && countTaskInProgress == 0) {
                newStatus = TaskStatus.NEW;
            }
            setStatus(newStatus);
        }
    }

    // Удаление подзадачи из списка эпика
    public void removeSubtask(Subtask subtask) {
        int index = 0;
        for (Subtask s : subtaskList) {
            if(s.equals(subtask)) {
                subtaskList.remove(index);
                break;
            }
            index++;
        }
    }
}
