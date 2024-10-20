import exceptions.LoadException;
import exceptions.SaveException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    //  private static FileBackedTaskManager manager;
    private String filename;
    private File tmpFile;

    FileBackedTaskManagerTest() {
        super(new FileBackedTaskManager());
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
        try {
            tmpFile = File.createTempFile("testdata", ".csv");
            filename = tmpFile.getAbsolutePath();
            manager = new FileBackedTaskManager(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    "Description task1",
                    LocalDateTime.now(),
                    Duration.ofMinutes(15)));
            assertTrue(tmpFile.length() > 0,
                    "данные не сохранены.\n" + filename);
        } catch (SaveException e) {
            System.out.println(e.getDetailMessage());
        }
    }

    /**
     * Тестируем загрузку задач из файла
     */
    @Test
    void loadFromFile() {
        int taskId = manager.addNewTask(new Task("Test loadFromFile Task1",
                "task1",
                LocalDateTime.now(),
                Duration.ofMinutes(15)));
        int epicId = manager.addNewEpic(new Epic("Test loadFromFile Epic1",
                "epic1"));
        int subtaskId = manager.addNewSubtask(new Subtask(epicId, "Test loadFromFile Subtask 1",
                "subtask1",
                LocalDateTime.now().plusMinutes(20),
                Duration.ofMinutes(15)));
        taskId = manager.addNewTask(new Task("Test loadFromFile Task2",
                "task2",
                LocalDateTime.now().plusMinutes(40),
                Duration.ofMinutes(15)));

        // удаляем первую задачу что бы внести путаницу в идентификаторы
        manager.removeTask(1);

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

        final Subtask subtaskEtalon = manager.getSubtask(subtaskId);
        Subtask subtaskFromFile = manager2.getSubtask(subtaskId);
        assertEquals(subtaskEtalon, subtaskFromFile,
                "Загруженная подзадача не соответствует сохраненной.");

        taskId = manager2.addNewTask(new Task("Test loadFromFile Task3",
                "task3",
                LocalDateTime.now().plusMinutes(80),
                Duration.ofMinutes(15)));
        assertEquals(5, taskId,
                "Некорректный счетчмк идентификаторов после загрузки файла.");
    }

    /**
     * Тестируем сохранение и загрузку пустого менеджера задач.
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

    /**
     * Тестируем чтение из пустого файла.
     */
    @Test
    void loadEmptyFile() {
        FileBackedTaskManager manager2;
        tmpFile = new File(filename);

        try {
            tmpFile.createNewFile(); // создаем пустой файл
            manager2 = FileBackedTaskManager.loadFromFile(tmpFile);
            assertNotNull(manager2, "Ошибка создания менеджера задач.");

            int taskId = manager2.addNewTask(new Task("Task 1",
                    "Description task 1",
                    LocalDateTime.now(),
                    Duration.ofMinutes(15)));
            assertEquals(1, manager2.getNumberOfObjects(),
                    "Ошибка работы с созданным менеджером.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Тестируем исключение при сохранении задач в файл
     */
    @Test
    void testSaveException() {
        Task task = new Task("Test testSaveException Task1",
                "task1",
                LocalDateTime.now(),
                Duration.ofMinutes(20));

        // задаем некорректное имя файла для сохранения
        manager.setSaveFileName("");
        assertThrows(SaveException.class,
                () -> {
                    manager.addNewTask(task);
                },
                "Запись задач в несуществующий файл должна приводить к исключению.");
    }

    /**
     * Тестируем исключение при загрузке данных из файла
     */
    @Test
    void testLoadException() {
        // пишем во временный файл задачу с искаженным временем запуска
        try (FileWriter fileWriter = new FileWriter(filename)) {
            fileWriter.write("id;DateTime;Duration(min);type;name;status;description;epic\n");
            fileWriter.write("1;2024.12.31 99:99;10;TASK;Задача №0;NEW;С новым годом!;\n");
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertThrows(LoadException.class,
                () -> {
                    FileBackedTaskManager.loadFromFile(tmpFile);
                },
                "Неверный формат времени при загрузке должен привести к исключению.");
    }
}