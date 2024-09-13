import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private static FileBackedTaskManager manager;
    private static String filename;
    private static File tmpFile;

    @BeforeAll
    public static void beforeAll() {
        try {
            tmpFile = File.createTempFile("testdata", ".csv");
            filename = tmpFile.getAbsolutePath();
            manager = new FileBackedTaskManager(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void beforeEach() {
        manager.clear();
    }

    @AfterEach
    void afterEach() {
        tmpFile.delete();
    }

    /**
     * Тестируем функцию сохранения задач в файл
     */
    @Test
    void save() {
        try {
            tmpFile = new File(filename);
            // добавляем простую задачу
            int taskId = manager.addNewTask(new Task("Task 1",
                    "Description task1"));
            assertTrue(tmpFile.length() > 0,
                    "данные не сохранены.\n" + filename);
        } catch (ManagerSaveException e) {
            System.out.println(e.getDetailMessage());
        }
    }

    /**
     * Тестируем загрузку задач из файла
     */
    @Test
    void loadFromFile() {
        int taskId = manager.addNewTask(new Task("Task 1",
                "Description task 1"));
        int epicId = manager.addNewEpic(new Epic("Epic 1",
                "Description epic 1"));
        int subtaskId = manager.addNewSubtask(new Subtask(epicId, "Subtask 1",
                "Description subtask 1"));
        taskId = manager.addNewTask(new Task("Task 2",
                "Description task 2"));
        // удаляем первую задачу что бы внести путаницу в идентификаторы
        manager.removeTask(0);

        tmpFile = new File(filename);
        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(tmpFile);

        final Task taskEtalon = manager.getTask(taskId);
        Task taskFromFile = manager2.getTask(taskId);
        assertEquals(taskEtalon, taskFromFile,
                "Загруженная задача не соответствует сохраненной.");

        final Epic epicEtalon = manager.getEpic(epicId);
        Epic epicFromFile = manager2.getEpic(epicId);
        assertEquals(epicEtalon, epicFromFile,
                "Загруженный эпик не соответствует сохраненному.");

        final Subtask subtaskEtalon = manager.getSubtasks(subtaskId);
        Subtask subtaskFromFile = manager2.getSubtasks(subtaskId);
        assertEquals(subtaskEtalon, subtaskFromFile,
                "Загруженная подзадача не соответствует сохраненной.");

        taskId = manager2.addNewTask(new Task("Task 3", "Description task 3"));
        assertEquals(4, taskId,
                "Некорректный счетчмк идентификаторов после загрузки файла.");
    }

    /**
     * Тестируем тестируем сохранение и загрузку пустого менеджера задач.
     */
    @Test
    void saveAndLoadEmptyManager() {
        int taskId = manager.addNewTask(new Task("Task 1",
                "Description task 1"));
        manager.removeTask(taskId);
        assertEquals(0, manager.getNumberOfObjects(),
                "Список задач не пуст.");

        tmpFile = new File(filename);
        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(tmpFile);
        assertEquals(0, manager2.getNumberOfObjects(),
                "Загруженный список задач не пуст.");

    }

    @Test
    void loadEmptyFile() {
        FileBackedTaskManager manager2;
        tmpFile = new File(filename);

        try {
            tmpFile.createNewFile(); // создаем пустой файл
            manager2 = FileBackedTaskManager.loadFromFile(tmpFile);
            assertNotNull(manager2, "Ошибка создания менеджера задач.");
            int taskId = manager2.addNewTask(new Task("Task 1",
                    "Description task 1"));
            assertEquals(1, manager2.getNumberOfObjects(),
                    "Ошибка работы с созданным менеджером.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}