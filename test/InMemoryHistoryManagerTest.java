import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {
    private static HistoryManager historyManager;

    @BeforeAll
    public static void beforeAll() {
        historyManager = Managers.getDefaultHistory();
    }

    @BeforeEach
    void beforeEach() {
        historyManager.clear();
    }

    /**
     * Тестирование добавления задачи в историю
     * и обновлениея записи если задача добавляется с уже существующим идентификатором
     */
    @Test
    public void add() {
        Task task = new Task("Test addTasktoHistory");
        task.setId(1);

        historyManager.add(task);

        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не читается.");
        assertEquals(1, history.size(),
                "Число записей в истории не соответствует ожидаемому.");

        // для задачи с измененным идентификатором должна быть создана отдельная запись истории
        task.setId(2);
        task.setDescription("идентификатор задачи изменен");
        historyManager.add(task);
        assertEquals(2, historyManager.getHistory().size(),
                "История не пополняется.");

        // для задачи с существующим идентификатором запись истории должна перезаписаться
        task.setStatus(TaskStatus.DONE);
        historyManager.add(task);
        assertEquals(TaskStatus.DONE, historyManager.getHistory().get(1).getStatus(),
                "История не обновляется.");
    }

    /**
     * Тестированее содержимого истории
     * Если объект задачи после добавления в историю изменяется,
     * то запись в истории не должна меняться до очередного обновления или добавления.
     */
    @Test
    public void getHistory() {
        Task task = new Task("Test getHistory 1");
        task.setId(1);
        historyManager.add(task);

        // копируем из истории первоначальную запись о задаче
        final Task taskEtalon = new Task(historyManager.getHistory().getFirst());

        // изменяем первоначальную задачу
        task.setId(2);
        task.setDescription("меняем оописание задачи");

        // проверяем неизменность записи задачи в истории после измениия объекта задачи
        // первй элемент в истории должен остаться неизменным
        assertEquals(taskEtalon, historyManager.getHistory().getFirst(),
                "История искажена.");
    }

    @Test
    public void remove() {
        Task task = new Task("History  task 1");
        task.setId(1);
        historyManager.add(task);
        task = new Task("History  task 2");
        task.setId(2);
        historyManager.add(task);
        task = new Task("History  task 3");
        task.setId(3);
        historyManager.add(task);

        assertEquals(3, historyManager.getHistory().size(),
                "История не пополняется.");

        // удаляем задачу из середины истории
        historyManager.remove(2);
        assertEquals(3, historyManager.getHistory().get(1).getId(),
                "непредвиденная последовательность при удалении элемента \"середина\".");

        // удаляем задачу с конца истории
        historyManager.remove(3);
        assertEquals(1, historyManager.getHistory().get(0).getId(),
                "непредвиденная последовательность при удалении элемента \"конец\".");

        task = new Task("History  task 4");
        task.setId(4);
        historyManager.add(task);

        // удаляем задачу с конца истории
        historyManager.remove(1);
        assertEquals(4, historyManager.getHistory().get(0).getId(),
                "непредвиденная последовательность при удалении элемента \"начало\".");
    }
}