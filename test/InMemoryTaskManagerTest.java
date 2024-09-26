import ManagerExceptions.TaskCrossTimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    // инициализируем родительский класс
    InMemoryTaskManagerTest() {
        super(new InMemoryTaskManager());
    }

    @BeforeEach
    void beforeEach() {
        manager.clear();
    }

    /**
     * Тестируем расчет статуса эпика
     */
    @Test
    void statusEpic() {
        final int epicId = manager.addNewEpic(new Epic("Test epicStatus Epic",
                "Epic"));
        assertEquals(TaskStatus.NEW, manager.getEpic(epicId).getStatus(),
                "Не ожидаемый статус у нового эпика.");

        int subtaskId1 = manager.addNewSubtask(new Subtask(epicId, "Test epicStatus Subtask1",
                "subtask1",
                LocalDateTime.now(),
                Duration.ofMinutes(15)));
        int subtaskId2 = manager.addNewSubtask(new Subtask(epicId, "Test epicStatus Subtask1",
                "subtask1",
                LocalDateTime.now().plusMinutes(20),
                Duration.ofMinutes(15)));
        assertEquals(TaskStatus.NEW, manager.getEpic(epicId).getStatus(),
                "Не ожидаемый статус у эпика. (Все подзадачи - NEW)");

        Subtask subtask = new Subtask(manager.getSubtask(subtaskId2));
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epicId).getStatus(),
                "Не ожидаемый статус у эпика. (подзадачи - NEW,IN_PROGRESS)");

        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epicId).getStatus(),
                "Не ожидаемый статус у эпика. (подзадачи - NEW,DONE)");

        subtask = new Subtask(manager.getSubtask(subtaskId1));
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);
        assertEquals(TaskStatus.DONE, manager.getEpic(epicId).getStatus(),
                "Не ожидаемый статус у эпика. (подзадачи - DONE,DONE)");
    }

    /**
     * Тестируем конфликты времени при добавлении задач, подзадач.
     */
    @Test
    void timeConflict() {
        manager.addNewTask(new Task("Test timeConflict Task1",
                "task1",
                LocalDateTime.now().plusMinutes(20),
                Duration.ofMinutes(20)));

        // Создаем задачу с периодом перекрвыающим начало существующей задачи
        Task task = new Task("Test timeConflict Task2",
                "task2",
                LocalDateTime.now().plusMinutes(10),
                Duration.ofMinutes(20));
        assertThrows(TaskCrossTimeException.class,
                () -> {
                    manager.addNewTask(task);
                },
                "Наложение времени задач должно приводить к исключению.");

        // изменяем период задачи на перекрвыающий окончание существующей задачи
        task.setStartTime(LocalDateTime.now().plusMinutes(30));
        assertThrows(TaskCrossTimeException.class,
                () -> {
                    manager.addNewTask(task);
                },
                "Наложение времени задач должно приводить к исключению.");

        // изменяем период задачи на вложенный во время выполнения существующей задачи
        task.setDuration(Duration.ofMinutes(5));
        assertThrows(TaskCrossTimeException.class,
                () -> {
                    manager.addNewTask(task);
                },
                "Наложение времени задач должно приводить к исключению.");

        // изменяем период задачи на перекрвыающий все время выполнения существующей задачи
        task.setStartTime(LocalDateTime.now().plusMinutes(10));
        task.setDuration(Duration.ofMinutes(50));
        assertThrows(TaskCrossTimeException.class,
                () -> {
                    manager.addNewTask(task);
                },
                "Наложение времени задач должно приводить к исключению.");
    }
}