import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private static TaskManager manager;

    @BeforeAll
    public static void beforeAll() {
        manager = Managers.getDefault();
    }
    @Test
    public void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description");
        final int taskId = manager.addNewTask(task);

        final Task savedTask = manager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = manager.getTaskList();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    public void addNewEpic() {
        manager.removeAllEpics();
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description");
        final int epicId = manager.addNewEpic(epic);

        final Epic savedEpic = manager.getEpic(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = manager.getEpicList();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    public void addNewSubtask() {
        Epic epic = new Epic("Test addNewEpicForSubtask");
        final int epicId = manager.addNewEpic(epic);

        Subtask subtask = new Subtask(epicId, "Test addNewSubtask");
        final int subtaskId = manager.addNewSubtask(subtask);

        final Subtask savedSubtask = manager.getSubtasks(subtaskId);
        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");

        final List<Subtask> subtasks = manager.getSubtaskList();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.get(0), "Подзадачи не совпадают.");
    }

    /**
     * Проверка неизменности всех полей задачи после добавления в меджкр
     */
    @Test
    public void testTaskFildsUnchangeable() {
        Task task = new Task("Test TaskFildsUnchangeable");
        final int taskId = manager.addNewTask(task);
        final Task savedTask = manager.getTask(taskId);

        assertEquals(task.getId(), savedTask.getId(), "Идентификаторы задач различаются");
        assertEquals(task.getTitle(), savedTask.getTitle(), "Наименования задач раздичаются");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описания задач различаются");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статус задач различается");
    }
}