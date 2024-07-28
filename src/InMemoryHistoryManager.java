import tasks.*;
import java.util.List;
import java.util.LinkedList;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int HISTORY_MAX_SIZE = 10;
    private final List<Task> history;

    public InMemoryHistoryManager() {
        history = new LinkedList<>();
    }

    /**
     *  Для отражения истории измений задач
     *  добавляем в список не сам объект, а копию.
     *  Копия будет сохранена в истории и не будет изменятся
     *  при дальнейшем изменении первоначальной задачи.
     */

    @Override
    public void add(Task task) {
        if (task == null) return;
        if (history.size() ==  HISTORY_MAX_SIZE) {
            history.remove(0);
        }
        history.add(new Task(task));
    }

    @Override
    public void add(Epic epic) {
        if (epic == null) return;
        if (history.size() ==  HISTORY_MAX_SIZE) {
            history.remove(0);
        }
        history.add(new Epic(epic));
    }

    @Override
    public void add(Subtask subtask) {
        if (subtask == null) return;
        if (history.size() ==  HISTORY_MAX_SIZE) {
            history.remove(0);
        }
        history.add(new Subtask(subtask));
    }

    @Override
    public List<Task> getHistory() {
        return new LinkedList<>(history);
    }

    @Override
    public void clear() {
        history.clear();
    }
}
