import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    TaskManagerTest(T manager) {
        this.manager = manager;
    }

    // перед каждым тестом очищаем хранилище
    @BeforeEach
    void setUp() {
        manager.removeAllTasks();
        manager.removeAllEpics();
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask",
                "Test addNewTask description",
                LocalDateTime.now(),
                Duration.ofMinutes(30));
        final int taskId = manager.addNewTask(task);

        // для проверки загружаем копию задачи из хранилища
        final Task savedTask = new Task(manager.getTask(taskId));

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задача не совпадает с исходной.");

    }

    @Test
    void addNewEpic() {
        Epic epic = new Epic("Test addNewEpic",
                "Test addNewEpic description");
        final int epicId = manager.addNewEpic(epic);

        final Epic savedEpic = new Epic(manager.getEpic(epicId));

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпик не совпадает с исходным.");

        final List<Epic> epics = manager.getEpicList();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void addNewSubtask() {
        Epic epic = new Epic("Test addNewEpicForSubtask1");
        final int epicId = manager.addNewEpic(epic);

        Subtask subtask = new Subtask(epicId, "addNewSubtask Test1",
                "Test addNewSubtask",
                LocalDateTime.now(),
                Duration.ofMinutes(30));
        final int subtaskId = manager.addNewSubtask(subtask);

        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");

        final List<Subtask> subtasks = manager.getSubtaskList();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.get(0), "Подзадачи не совпадают.");
    }

    @Test
    void getTask() {
        Task task = new Task("Test getTask",
                "-",
                LocalDateTime.now(),
                Duration.ofMinutes(30));
        final int taskId = manager.addNewTask(task);
        final Task savedTask = new Task(manager.getTask(taskId));

        assertEquals(task, savedTask, "Задачи не совпадают.");
        assertEquals(task.getId(), savedTask.getId(), "Идентификаторы задач различаются");
        assertEquals(task.getTitle(), savedTask.getTitle(), "Наименования задач раздичаются");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описания задач различаются");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статус задач различается");
    }

    @Test
    void getEpic() {
        Epic epic = new Epic("Test getEpic",
                "Test addNewEpic description");
        final int epicId = manager.addNewEpic(epic);

        final Epic savedEpic = new Epic(manager.getEpic(epicId));

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");
    }

    @Test
    void getSubtask() {
        Epic epic = new Epic("Test addNewEpicForSubtask-2");
        final int epicId = manager.addNewEpic(epic);

        Subtask subtask = new Subtask(epicId, "addNewSubtask Test1",
                "Test addNewSubtask",
                LocalDateTime.now(),
                Duration.ofMinutes(30));
        final int subtaskId = manager.addNewSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");
        assertEquals(epicId, savedSubtask.getEpicId(),
                "У подзадачи изменен идентификатор эпика.");
    }

    @Test
    void updateTask() {
        final int taskId = manager.addNewTask(new Task("Test updateTask",
                "-",
                LocalDateTime.now(),
                Duration.ofMinutes(30)));
        Task task = new Task(manager.getTask(taskId));
        task.setDescription("Updated task");
        task.setStatus(TaskStatus.IN_PROGRESS);

        manager.updateTask(task);
        Task updatedTask = new Task(manager.getTask(taskId));

        assertNotNull(updatedTask, "Задача не обновляется.");
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus(),
                "Обновления задачи не сохранены.");

    }

    @Test
    void updateEpic() {

        final int epicId = manager.addNewEpic(new Epic("Test updateEpic",
                "-"));
        Epic epic = new Epic(manager.getEpic(epicId));
        epic.setDescription("Updated Epic.");
        epic.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateEpic(epic);

        final Epic updatedEpic = new Epic(manager.getEpic(epicId));

        assertNotNull(updatedEpic, "Эпик не обновляется.");
        assertTrue(updatedEpic.getDescription().equals("Updated Epic."),
                "Обновления эпика не сохранены.");
    }

    @Test
    void updateSubtask() {
        final int epicId = manager.addNewEpic(new Epic("Test addNewEpicForSubtask2"));

        final int subtaskId = manager.addNewSubtask(new Subtask(epicId, "Test updateSubtask",
                "-",
                LocalDateTime.now(),
                Duration.ofMinutes(30)));

        Subtask subtask = new Subtask(manager.getSubtask(subtaskId));
        subtask.setDescription("Updated Subtask.");
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);

        final Subtask updatedSubtask = new Subtask(manager.getSubtask(subtaskId));

        assertNotNull(updatedSubtask, "Подзадача не найдена.");
        assertTrue(updatedSubtask.getStatus().equals(TaskStatus.DONE),
                "Обновления подзадачи не сохранены.");
    }

    @Test
    void removeTask() {
        final int taskId = manager.addNewTask(new Task("Test removeTask",
                "-",
                LocalDateTime.now(),
                Duration.ofMinutes(10)));
        manager.removeTask(taskId);
        Task task = manager.getTask(taskId);

        assertNull(task, "Задача не удалена.");
    }

    @Test
    void removeEpic() {
        final int epicId = manager.addNewEpic(new Epic("Test removeEpic",
                "-"));
        final int subtaskId = manager.addNewSubtask(new Subtask(epicId,
                "Test removeEpic Subtask",
                "-",
                LocalDateTime.now(),
                Duration.ofMinutes(30)));

        manager.removeEpic(epicId);

        Epic epic = manager.getEpic(epicId);
        Subtask subtask = manager.getSubtask(subtaskId);

        assertNull(epic, "Эпик не удален.");
        assertNull(subtask, "Подзадача удаленного эпика не удалена.");
    }

    @Test
    void removeSubtask() {
        final int epicId = manager.addNewEpic(new Epic("Test removeSubtask Epic",
                "-"));
        final int subtaskId = manager.addNewSubtask(new Subtask(epicId,
                "Test removeSubtask Subtask",
                "-",
                LocalDateTime.now(),
                Duration.ofMinutes(30)));

        manager.removeSubtask(subtaskId);

        Subtask subtask = manager.getSubtask(subtaskId);

        assertNull(subtask, "Подзадача не удалена.");
    }

    @Test
    void getTaskList() {
        Task task = new Task("Test getTaskList 1",
                "1",
                LocalDateTime.now(),
                Duration.ofMinutes(30));
        int taskId = manager.addNewTask(task);

        taskId = manager.addNewTask(new Task("Test getTaskList 2",
                "2",
                LocalDateTime.now().plusMinutes(40),
                Duration.ofMinutes(30)));

        // получаем список задач
        final List<Task> tasks = manager.getTaskList();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(2, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");

    }

    @Test
    void getEpicList() {
        int epicId = manager.addNewEpic(new Epic("Test getEpicList Epic1",
                "Epic1"));
        epicId = manager.addNewEpic(new Epic("Test getEpicList Epic2",
                "Epic2"));

        final List<Epic> epics = manager.getEpicList();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(2, epics.size(), "Неверное количество эпиков.");
    }

    @Test
    void getSubtaskList() {
        final int epicId = manager.addNewEpic(new Epic("Test getSubtaskList Epic",
                "-"));
        manager.addNewSubtask(new Subtask(epicId,
                "Test getSubtaskList Subtask1",
                "1", LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.addNewSubtask(new Subtask(epicId,
                "Test getSubtaskList Subtask2",
                "2", LocalDateTime.now().plusMinutes(40), Duration.ofMinutes(30)));

        final List<Subtask> subtasks = manager.getSubtaskList();

        assertNotNull(subtasks, "подзадачи не возвращаются.");
        assertEquals(2, subtasks.size(), "Неверное количество подзадач.");
    }

    @Test
    void removeAllTasks() {
        manager.addNewTask(new Task("Test removeAllTasks task1",
                "1", LocalDateTime.now(), Duration.ofMinutes(30)));

        manager.addNewTask(new Task("Test removeAllTasks task2",
                "2", LocalDateTime.now().plusMinutes(40), Duration.ofMinutes(30)));

        manager.removeAllTasks();

        assertTrue(manager.getTaskList().isEmpty(), "Задачи не удалены.");
    }

    @Test
    void removeAllEpics() {
        manager.addNewEpic(new Epic("Test removeAllEpics Epic1",
                "-"));
        final int epicId = manager.addNewEpic(new Epic("Test removeAllEpics Epic2",
                "-"));
        manager.addNewSubtask(new Subtask(epicId,
                "Test removeAllEpics Subtask1",
                "1", LocalDateTime.now(), Duration.ofMinutes(30)));

        manager.removeAllEpics();

        assertTrue(manager.getEpicList().isEmpty(), "Эпики не удалены.");
        assertTrue(manager.getSubtaskList().isEmpty(),
                "Подзадачи удаленных эпиков не удалены.");
    }

    @Test
    void removeAllSubtasks() {
        final int epicId = manager.addNewEpic(new Epic("Test removeAllSubtasks Epic1",
                "-"));
        manager.addNewSubtask(new Subtask(epicId,
                "Test removeAllSubtasks Subtask1",
                "1", LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.addNewSubtask(new Subtask(epicId,
                "Test removeAllSubtasks Subtask2",
                "2", LocalDateTime.now().plusMinutes(40), Duration.ofMinutes(30)));

        final int epicId2 = manager.addNewEpic(new Epic("Test removeAllSubtasks Epic2",
                "-"));
        manager.addNewSubtask(new Subtask(epicId2,
                "Test removeAllSubtasks Subtask3",
                "3", LocalDateTime.now().plusMinutes(80), Duration.ofMinutes(30)));

        manager.removeAllSubtasks();

        assertTrue(manager.getSubtaskList().isEmpty(), "Подзадачи не удалены.");
        assertTrue(manager.getEpic(epicId).getSubtasks().isEmpty(),
                "не удалены идениификаторы подзадач эпика:\n"
                        + manager.getEpic(epicId).toString());
        assertTrue(manager.getEpic(epicId2).getSubtasks().isEmpty(),
                "не удалены идениификаторы подзадач эпика:\n"
                        + manager.getEpic(epicId2).toString());
    }

    @Test
    void getSubtasksByEpic() {
        final int epicId = manager.addNewEpic(new Epic("Test getSubtasksByEpic Epic1",
                "-"));
        manager.addNewSubtask(new Subtask(epicId,
                "Test getSubtasksByEpic Subtask1",
                "1", LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.addNewSubtask(new Subtask(epicId,
                "Test getSubtasksByEpic Subtask2",
                "2", LocalDateTime.now().plusMinutes(40), Duration.ofMinutes(30)));

        final int epicId2 = manager.addNewEpic(new Epic("Test getSubtasksByEpic Epic2",
                "-"));
        manager.addNewSubtask(new Subtask(epicId2,
                "Test getSubtasksByEpic Subtask3",
                "3", LocalDateTime.now().plusMinutes(80), Duration.ofMinutes(30)));
        manager.addNewSubtask(new Subtask(epicId2,
                "Test getSubtasksByEpic Subtask4",
                "4", LocalDateTime.now().plusMinutes(120), Duration.ofMinutes(30)));

        final List<Subtask> subtasks = manager.getSubtasksByEpic(epicId2);

        assertNotNull(subtasks, "подзадачи не возвращаются.");
        assertEquals(2, subtasks.size(), "Неверное количество подзадач.");
    }

    @Test
    void getHistory() {
        final int taskId = manager.addNewTask(new Task("Test getHistory task1",
                "1", LocalDateTime.now(), Duration.ofMinutes(30)));
        final int epicId = manager.addNewEpic(new Epic("Test getHistory Epic1",
                "1"));
        final int subtaskId = manager.addNewSubtask(new Subtask(epicId,
                "Test getHistory Subtask1",
                "1", LocalDateTime.now().plusMinutes(40), Duration.ofMinutes(30)));
        String history = manager.getTask(taskId).toString() + "\n";
        history += manager.getEpic(epicId).toString() + "\n";
        history += manager.getSubtask(subtaskId);

        List<Task> historyList = manager.getHistory();

        assertNotNull(historyList, "История объектов не возвращается");
        assertEquals(3, historyList.size(), "История не полная.\n" + history);
    }

    @Test
    void getPrioritizedTasks() {
        manager.addNewTask(new Task("Test getPrioritizedTasks task1",
                "1", LocalDateTime.now().plusMinutes(120), Duration.ofMinutes(30)));
        final int epicId = manager.addNewEpic(new Epic("Test getPrioritizedTasks Epic1",
                "1"));
        manager.addNewSubtask(new Subtask(epicId,
                "Test getPrioritizedTasks Subtask1",
                "1", LocalDateTime.now().plusMinutes(45), Duration.ofMinutes(10)));
        manager.addNewSubtask(new Subtask(epicId,
                "Test getPrioritizedTasks Subtask2",
                "2", LocalDateTime.now().plusMinutes(60), Duration.ofMinutes(10)));
        manager.addNewTask(new Task("Test getPrioritizedTasks task2",
                "2", LocalDateTime.now(), Duration.ofMinutes(30)));

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        //    prioritizedTasks.addLast(new Task("123", "искуственно нарушаем порядок задач",
        //            LocalDateTime.now().plusMinutes(35), Duration.ofMinutes(5)));

        assertNotNull(prioritizedTasks,
                "Список задач сортированных по времени не возвращается");

        for (int i = 1; i < prioritizedTasks.size(); i++) {
            Task task1 = prioritizedTasks.get(i - 1);
            Task task2 = prioritizedTasks.get(i);
            assertTrue(task1.getStartTime().isBefore(task2.getStartTime()),
                    "Нарушена последовательность задач :\n"
                            + task1.toString() + "\n"
                            + task2.toString() + "\n");
        }
    }
}