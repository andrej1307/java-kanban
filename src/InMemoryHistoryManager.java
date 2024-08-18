import tasks.Task;
import util.Node;
import util.SimpleLinkedList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final SimpleLinkedList<Task> historyList;
    private final HashMap<Integer, Node<Task>> historyMap;

    public InMemoryHistoryManager() {
        historyList = new SimpleLinkedList<>();
        historyMap = new HashMap<>();
    }

    /**
     *  Для отражения истории измений задач
     *  добавляем в список не сам объект, а копию.
     *  Копия будет сохранена в истории и не будет изменятся
     *  при дальнейшем изменении первоначальной задачи.
     */

    /**
     * добавление задачи в конец связанного списка истории просмотров
     *
     * @param task - задача для добавления или обновления
     */
    @Override
    public void add(Task task) {
        if (task == null) return;
        int taskId = task.getId();
        Node<Task> newNode = new Node(historyList.getTail(), new Task(task), null);
        if (historyMap.containsKey(taskId)) {
            historyList.removeNode(historyMap.get(taskId));
        }
        historyMap.put(taskId, newNode);
        historyList.addLastNode(newNode);
    }

    /**
     * Удаление задачи из списка истори
     *
     * @param taskId - идентификатор задачи
     */
    public void remove(int taskId) {
        if (historyMap.containsKey(taskId)) {
            historyList.removeNode(historyMap.get(taskId));
            historyMap.remove(taskId);
        }
    }

    /**
     * Чтение списка истории чтения задач
     *
     * @return - список просмоотренных задач
     */
    @Override
    public List<Task> getHistory() {
        List<Task> taskList = new ArrayList<>();
        for (Task task : historyList) {
            taskList.add(task);
        }
        return taskList;
    }

    public void clear() {
        historyList.clear();
        historyMap.clear();
    }
}
