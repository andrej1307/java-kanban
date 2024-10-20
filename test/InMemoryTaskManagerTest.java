import exceptions.TaskCrossTimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

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
                "Неожидаемый статус у нового эпика.");

        int subtaskId1 = manager.addNewSubtask(new Subtask(epicId, "Test epicStatus Subtask1",
                "subtask1",
                LocalDateTime.now(),
                Duration.ofMinutes(15)));
        int subtaskId2 = manager.addNewSubtask(new Subtask(epicId, "Test epicStatus Subtask1",
                "subtask1",
                LocalDateTime.now().plusMinutes(20),
                Duration.ofMinutes(15)));
        assertEquals(TaskStatus.NEW, manager.getEpic(epicId).getStatus(),
                "Неожидаемый статус у эпика. (Все подзадачи - NEW)");

        Subtask subtask = new Subtask(manager.getSubtask(subtaskId2));
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epicId).getStatus(),
                "Неожидаемый статус у эпика. (подзадачи - NEW,IN_PROGRESS)");

        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epicId).getStatus(),
                "Неожидаемый статус у эпика. (подзадачи - NEW,DONE)");

        subtask = new Subtask(manager.getSubtask(subtaskId1));
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);
        assertEquals(TaskStatus.DONE, manager.getEpic(epicId).getStatus(),
                "Неожидаемый статус у эпика. (подзадачи - DONE,DONE)");
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
                "Наложение времени задач должно приводить к исключению.\n"
                        + task.toString());

        // изменяем период задачи на перекрвыающий окончание существующей задачи
        task.setStartTime(LocalDateTime.now().plusMinutes(30));
        assertThrows(TaskCrossTimeException.class,
                () -> {
                    manager.addNewTask(task);
                },
                "Наложение времени задач должно приводить к исключению.\n"
                        + task.toString());

        // изменяем период задачи на вложенный во время выполнения существующей задачи
        task.setDuration(Duration.ofMinutes(5));
        assertThrows(TaskCrossTimeException.class,
                () -> {
                    manager.addNewTask(task);
                },
                "Наложение времени задач должно приводить к исключению.\n"
                        + task.toString());

        // изменяем период задачи на перекрвыающий все время выполнения существующей задачи
        task.setStartTime(LocalDateTime.now().plusMinutes(10));
        task.setDuration(Duration.ofMinutes(50));
        assertThrows(TaskCrossTimeException.class,
                () -> {
                    manager.addNewTask(task);
                },
                "Наложение времени задач должно приводить к исключению.\n"
                        + task.toString());
    }

    /**
     * Тестируем расчет времени и продолжительность эпика
     */
    @Test
    void epicTime() {
        Epic epic = new Epic("Test epicTime Epic", "Epic");
        final int epicId = manager.addNewEpic(epic);
        assertNull(epic.getStartTime(),
                "Время начала работы эпика без подзадач должно быть null");

        int subtaskId1 = manager.addNewSubtask(new Subtask(epicId, "Test epicTime Subtask1",
                "subtask1",
                LocalDateTime.now().plusMinutes(20),
                Duration.ofMinutes(10)));
        int subtaskId2 = manager.addNewSubtask(new Subtask(epicId, "Test epicTime Subtask2",
                "subtask2",
                LocalDateTime.now().plusMinutes(40),
                Duration.ofMinutes(15)));
        int subtaskId3 = manager.addNewSubtask(new Subtask(epicId, "Test epicTime Subtask3",
                "subtask3",
                LocalDateTime.now(),
                Duration.ofMinutes(15)));

        LocalDateTime expectedStartTime = manager.getSubtask(subtaskId3).getStartTime();
        LocalDateTime expectedEndIime = manager.getSubtask(subtaskId2).getEndTime();
        int expectedDuration = 10 + 15 + 15;

        assertEquals(expectedStartTime, epic.getStartTime(),
                "Время начала эпика не соответствует расчетному.\n"
                        + epic.toString());
        assertEquals(expectedEndIime, epic.getEndTime(),
                "Время завершения эпика не соответствует расчетному.\n"
                        + epic.toString());
        assertEquals(expectedDuration, epic.getDuration().toMinutes(),
                "Продолжительность эпика не соответствует расчетной\n"
                        + epic.toString());
    }

    /**
     * Тестируем удаление задачи из отсоритрованного списка
     */
    @Test
    void removeSortedTask() {
        int taskId1 = manager.addNewTask(new Task("Test reoveSortedTask task1",
                "task1",
                LocalDateTime.now(),
                Duration.ofMinutes(10)));
        final int taskId2 = manager.addNewTask(new Task("Test reoveSortedTask task2",
                "task2",
                LocalDateTime.now().plusMinutes(15),
                Duration.ofMinutes(10)));
        final int taskId3 = manager.addNewTask(new Task("Test reoveSortedTask task3",
                "taks3",
                LocalDateTime.now().plusMinutes(30),
                Duration.ofMinutes(10)));

        int expectedSortedSize = manager.getPrioritizedTasks().size() - 1;
        manager.removeTask(taskId1);

        assertEquals(expectedSortedSize, manager.getPrioritizedTasks().size(),
                "Ошибка удаления задачи из отсортированного списка задач.");
    }

    /**
     * Тестируем удаление подзадачи из отсоритрованного списка
     */
    @Test
    void removeSortedSubtask() {
        Epic epic = new Epic("Test removeSortedSubtask Epic", "Epic");
        final int epicId = manager.addNewEpic(epic);
        assertNull(epic.getStartTime(),
                "Время начала работы эпика без подзадач должно быть null");

        int subtaskId1 = manager.addNewSubtask(new Subtask(epicId, "Test removeSortedSubtask Subtask1",
                "subtask1",
                LocalDateTime.now().plusMinutes(20),
                Duration.ofMinutes(10)));
        int subtaskId2 = manager.addNewSubtask(new Subtask(epicId, "Test removeSortedSubtask Subtask2",
                "subtask2",
                LocalDateTime.now().plusMinutes(40),
                Duration.ofMinutes(15)));
        int subtaskId3 = manager.addNewSubtask(new Subtask(epicId, "Test removeSortedSubtask Subtask3",
                "subtask3",
                LocalDateTime.now(),
                Duration.ofMinutes(15)));

        int expectedSortedSize = manager.getPrioritizedTasks().size() - 1;
        manager.removeSubtask(subtaskId2);

        assertEquals(expectedSortedSize, manager.getPrioritizedTasks().size(),
                "Ошибка удаления подзадачи из отсортированного списка задач.");
    }
}