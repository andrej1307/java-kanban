import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTasksHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = taskServer.getGson();

    public HttpTasksHandlerTest() {
    }

    @BeforeEach
    void setUp() throws IOException {
        manager.removeAllTasks();
        manager.removeAllEpics();
        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    /**
     * Тестируем добавление задачи через Http сервер
     */
    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task( "Test AddTask",
                "Testing AddTask",
                LocalDateTime.now(),
                Duration.ofMinutes(15));

        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode(),
                "Добавление задачи: неожиданный код ответа сервера");

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getTaskList();

        assertNotNull(tasksFromManager,
                "Задача не создана");
        assertEquals(1, tasksFromManager.size(),
                "Некорректное количество задач");
        assertEquals("Test AddTask", tasksFromManager.get(0).getTitle(),
                "Некорректное имя задачи");
    }

    /**
     * Тестируем обновление задачи через Http сервер
     */
    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        // добавляем задачу
        makeTaskList(1);

        Task task = new Task( "Test UpdateTask",
                "Testing UpdateTask",
                LocalDateTime.now(),
                Duration.ofMinutes(15));

        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode(),
                "Обновление задачи: неожиданный код ответа сервера");

        // читаем,  задачу из хранилища
        List<Task> tasksFromManager = manager.getTaskList();

        assertNotNull(tasksFromManager,
                "Задача не создана");
        assertEquals(1, tasksFromManager.size(),
                "Некорректное количество задач");
        assertEquals("Test UpdateTask", tasksFromManager.get(0).getTitle(),
                "Задача не обновилась.");

    }

    /**
     * Тестируем чтение задачи по заданному идентификатору
     */
    @Test
    public void testGetTask() throws IOException, InterruptedException {
        // добавляем задачу
        makeTaskList(1);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode(),
                "Чтение задачи: неожиданный код ответа сервера");

        if (response.statusCode() == 200) {
            JsonElement jsonElement = JsonParser.parseString(response.body());
            assertTrue(jsonElement.isJsonObject(),
                    "Ошибка чтения ответа сервера.");
            Task newTask = null;
            newTask = gson.fromJson(jsonElement, Task.class);
            assertNotNull(newTask, "Ошибка десериализации задачи.");
            assertEquals(1, newTask.getId(),
                    "Неожиданный идентификатор задачи");
        }
    }

    /**
     * Вспомогательный класс определение типа объекта десериализации
     */
    class TaskListTypeToken extends TypeToken<List<Task>> {
    }

    /**
     * Тестируем чтение списка задач через HTTP сервер
     */
    @Test
    public void testGetTasks()  throws IOException, InterruptedException {
        // создаем список задач в менеджере
        makeTaskList(3);

        // создаём HTTP-клиент
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode(),
                "Чтение списка задач: неожиданный код ответа сервера");

        if (response.statusCode() == 200) {
            List<Task> taskListFromServer = gson.fromJson(response.body(), new TaskListTypeToken().getType());

            assertNotNull(taskListFromServer,
                    "Список задач не прочитан.");

            int expectedSize = manager.getTaskList().size();
            assertEquals(expectedSize, taskListFromServer.size(),
                    "Список задач не полный.");
        }
    }

    /**
     * Тестируем удаление задачи
     */
    @Test
    public void testDelete() throws IOException, InterruptedException {
        // создаем список задач
        makeTaskList(3);

        // создаём HTTP-клиент
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/2");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode(),
                "Удаление задачи: неожиданный код ответа сервера");

        assertEquals(2, manager.getTaskList().size(),
                "Неверная длина списка задач после удаления.");
    }

    /**
     * Тестируем добавление задачи пересекающейся по времени
     */
    @Test
    public void testTimeIntersection() throws IOException, InterruptedException {
        // создаем список задач
        makeTaskList(3);

        // создаем задачу с текущим временем
        Task task = new Task( "Test TimeIntersection",
                "Testing TimeIntersection",
                LocalDateTime.now(),
                Duration.ofMinutes(15));

        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(),
                "Добавление пересекающейся задачи: неожиданный код ответа сервера");
        assertEquals(3, manager.getTaskList().size(),
                "Неверная длина списка задач.");
    }

    /**
     * Тестируем ошибку 404
     */
    @Test
    public void testError404() throws IOException, InterruptedException {
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/100");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).GET().build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode(),
                "Чтение несуществующей задачи: неожиданный код ответа сервера");
    }

    /**
     * Тестируем ошибку 406
     */
    @Test
    public void testError406() throws IOException, InterruptedException {
        makeTaskList(3);

        Task task = new Task("Test timeIntersection Task",
                "timeIntersection",
                LocalDateTime.now(),
                Duration.ofMinutes(15));

        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(406, response.statusCode(),
                "Задача в недопустимом интервале: неожиданный код ответа сервера");
    }

    /**
     * Генерация набора задач в менеджере напрямую
     *
     * @param size - число генерируемых задач
     */
    private void makeTaskList(int size) {
        if(size == 0) {
            return;
        }
        for (int i = 0; i < size; i ++) {
            manager.addNewTask(new Task("taskList element:" + i,
                    "Testing taskList",
                    LocalDateTime.now().plusMinutes(20L *i),
                    Duration.ofMinutes(10)));
        }
    }
}