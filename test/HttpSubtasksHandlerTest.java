import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpSubtasksHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = taskServer.getGson();


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
     * Тестируем создание подзадачи через HTTP сервер
     */
    @Test
    public void testAddSubtask() throws IOException, InterruptedException {

        int epicId = manager.addNewEpic(new Epic("Test subtask epic",
                "Epic for testing Sabtask"));

        Subtask subtask = new Subtask(epicId, "Test AddSubtask", "-",
                LocalDateTime.now(),
                Duration.ofMinutes(15));

        String subtaskJson = gson.toJson(subtask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode(),
                "Добавление подзадачи: неожиданный код ответа сервера");

        // проверяем, что создана подзадача с корректным именем
        List<Subtask> subtasksFromManager = manager.getSubtaskList();

        assertNotNull(subtasksFromManager,
                "Подзадача не создана");
        assertEquals(1, subtasksFromManager.size(),
                "Некорректное количество подзадач");
        assertEquals("Test AddSubtask", subtasksFromManager.get(0).getTitle(),
                "Некорректное имя подзадачи");
    }

    /**
     * Тестируем обновление подзадачи через HTTP сервер
     */
    @Test
    public void tetUpdateSubtask() throws IOException, InterruptedException {
        int epicId = manager.addNewEpic(new Epic("Test subtask epic",
                "Epic for testing Sabtask"));

        int subtaskId = manager.addNewSubtask(new Subtask(epicId, "Test UpdateSubtask",
                "-",
                LocalDateTime.now(),
                Duration.ofMinutes(15)));

        // создаем подзадачу с измененными данными для обновления
        Subtask subtask = new Subtask(epicId, "Subtask Updated",
                "subtask updated successfully",
                LocalDateTime.now().plusMinutes(30),
                Duration.ofMinutes(15));
        subtask.setId(subtaskId);

        String subtaskJson = gson.toJson(subtask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode(),
                "обновление подзадачи: неожиданный код ответа сервера");

        // проверяем, что подзадача обновлена
        List<Subtask> subtasksFromManager = manager.getSubtaskList();

        assertNotNull(subtasksFromManager,
                "Подзадача не создана");
        assertEquals(1, subtasksFromManager.size(),
                "Некорректное количество подзадач");
        assertEquals("Subtask Updated", subtasksFromManager.get(0).getTitle(),
                "Подзадача не обновилась");
    }

    /**
     * Тестируем чтение подзадачи через HTTP сервер
     */
    @Test
    public void testGetSubtask() throws IOException, InterruptedException {
        makeSubtaskList(3);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).GET().build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode(),
                "Чтение подзадачи: неожиданный код ответа сервера");

        if (response.statusCode() == 200) {
            JsonElement jsonElement = JsonParser.parseString(response.body());
            assertTrue(jsonElement.isJsonObject(),
                    "Ошибка чтения ответа сервера.");
            Subtask newSubtask = null;
            newSubtask = gson.fromJson(jsonElement, Subtask.class);
            assertNotNull(newSubtask, "Ошибка десериализации подзадачи.");
            assertEquals(2, newSubtask.getId(),
                    "Неожиданный идентификатор подзадачи");
        }
    }

    class SubtaskListTypeToken extends TypeToken<List<Subtask>> {
    }

    /**
     * Тестируем чтение списка подзадач
     */
    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        // создаем набор подзадач
        makeSubtaskList(5);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).GET().build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode(),
                "Чтение подзадач: неожиданный код ответа сервера");

        if (response.statusCode() == 200) {
            List<Subtask> subtaskListFromServer = gson.fromJson(response.body(),
                    new SubtaskListTypeToken().getType());

            assertNotNull(subtaskListFromServer,
                    "Список подзадач не прочитан.");

            int expectedSize = manager.getSubtaskList().size();
            assertEquals(expectedSize, subtaskListFromServer.size(),
                    "Список подзадач не полный.");
        }
    }

    /**
     * Тестируем удаление подзадачи через HTTP сервер
     */
    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        makeSubtaskList(4);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/3");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).DELETE().build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode(),
                "удаление подзадачи: неожиданный код ответа сервера");

        assertEquals(3, manager.getSubtaskList().size(),
                "Неверная длина списка подзадач после удаления.");
    }

    /**
     * Тестируем ошибку 404
     */
    @Test
    public void testError404() throws IOException, InterruptedException {
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/100");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).GET().build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode(),
                "Чтение несуществующей подзадачи: неожиданный код ответа сервера");
    }

    /**
     * Тестируем ошибку 406
     */
    @Test
    public void testError406() throws IOException, InterruptedException {
        makeSubtaskList(3);

        Subtask subtask = new Subtask(1, "Test timeIntersection Subtask",
                "timeIntersection",
                LocalDateTime.now(),
                Duration.ofMinutes(15));

        String subtaskJson = gson.toJson(subtask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(406, response.statusCode(),
                "Подзадача в недопустимом интервале: неожиданный код ответа сервера");
    }

    /**
     * Метод генерации заданного числа подзадач
     *
     * @param size - число подзадач для генерации
     */
    private void makeSubtaskList(int size) {
        if (size == 0) {
            return;
        }
        int epicId = manager.addNewEpic(new Epic("Test subtask epic",
                "Epic for testing Sabtask"));

        for (int i = 0; i < size; i++) {
            manager.addNewSubtask(new Subtask(epicId,
                    "test Subtask:" + epicId + "." + (i + 1), "-",
                    LocalDateTime.now().plusMinutes(20 * i),
                    Duration.ofMinutes(15)));
        }
    }
}