import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private static HistoryManager historyManager;

    @BeforeAll
    public static void beforeAll() {
        historyManager= Managers.getDefaultHistory();
    }

    @Test
    public void add() {
        Task task = new Task("Test addTasktoHistory");
        task.setId(1);

        historyManager.add(task);

        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не читается.");
        assertEquals(1, history.size(),
                "Число записей в истории не соответствует ожидаемому.");
    }

    /**
     * проверяем неизменность записb задачи в истории после измениия объекта задачи
     */
    @Test
    public void getHistory() {
        historyManager.clear();

        Task task = new Task("Test history 1");
        task.setId(1);
        historyManager.add(task);

        // копируем из истории первоначальную запись о задаче
        final Task taskEtalon = new Task(historyManager.getHistory().get(0));

        // изменяем первоначальную задачу
        task.setId(2);
        task.setDescription("меняем оописание задачи");
        task.setStatus(TaskStatus.DONE);
        historyManager.add(task);

        // первй элемент в истории должен остаться неизменным
        assertEquals(taskEtalon, historyManager.getHistory().get(0),
                "История искажена.");
    }
}