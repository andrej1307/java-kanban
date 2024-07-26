import tasks.*;
import java.util.List;

public interface HistoryManager {
    // метод добавления события в историю
    void add(Task task);
    void add(Epic epic);
    void add(Subtask subtask);

    // метод получения списка событий мстории
    List<Task> getHistory();

    // метод очистки истории
    void clear();
}
