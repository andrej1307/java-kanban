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

class HttpEpicsHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = taskServer.getGson();


    @BeforeEach
    void setUp()  throws IOException {
        manager.removeAllTasks();
        manager.removeAllEpics();
        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    /**
     * Тестируем добавление эпика
     */
    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test AddEpic",
                "Teting AddNewEpic");

        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode(),
                "Добавление эпика: неожиданный код ответа сервера");

        // проверяем, что создался один эпик с корректным именем
        List<Epic> epicsFromManager = manager.getEpicList();

        assertNotNull(epicsFromManager,
                "Эпик не создан");
        assertEquals(1, epicsFromManager.size(),
                "Некорректное количество эпиков");
        assertEquals("Test AddEpic", epicsFromManager.get(0).getTitle(),
                "Некорректное имя эпика");

    }

    /**
     * Тестируем обновление информации об эпике через HTTP сервер
     */
    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        makeEpicList(1);

        Epic epic = new Epic("Test UpdateEpic",
                "Teting AddNewEpic");

        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode(),
                "Обновление эпика: неожиданный код ответа сервера");

        List<Epic> epicsFromManager = manager.getEpicList();

        assertNotNull(epicsFromManager,
                "Эпик не создан");
        assertEquals(1, epicsFromManager.size(),
                "Некорректное количество эпиков");
        assertEquals("Test UpdateEpic", epicsFromManager.get(0).getTitle(),
                "Эпик не обновлен");
    }

    /**
     * Тестируем чтение эпика через HTTP сервер
     */
    @Test
    public void tetGetEpic()  throws IOException, InterruptedException {
        makeEpicList(1);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).GET().build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode(),
                "Чтение эпика: неожиданный код ответа сервера");

        if (response.statusCode() == 200) {
            JsonElement jsonElement = JsonParser.parseString(response.body());
            assertTrue(jsonElement.isJsonObject(),
                    "Ошибка чтения ответа сервера.");
            Epic newEpic = null;
            newEpic = gson.fromJson(jsonElement, Epic.class);
            assertNotNull(newEpic, "Ошибка десериализации эпика.");
            assertEquals(1, newEpic.getId(),
                    "Неожиданный идентификатор эпика");
        }
    }

    /**
     * Вспомогательный класс определение типа объекта десериализации
     */
    class EpicListTypeToken extends TypeToken<List<Epic>> {
    }

    class SubtaskListTypeToken extends TypeToken<List<Subtask>> {
    }

    /**
     * Тестируем чтение списка эпиков черех HTTP сервер
     */
    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        makeEpicList(3);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).GET().build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode(),
                "Чтение эпика: неожиданный код ответа сервера");

        if (response.statusCode() == 200) {
            List<Epic> epicListFromServer = gson.fromJson(response.body(), new EpicListTypeToken().getType());

            assertNotNull(epicListFromServer,
                    "Список эпиков не прочитан.");

            int expectedSize = manager.getEpicList().size();
            assertEquals(expectedSize, epicListFromServer.size(),
                    "Список эпиков не полный.");
        }
    }

    /**
     *  Тестируем удаление эпика
     */
    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        makeEpicList(3);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).DELETE().build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode(),
                "Удаление эпика: неожиданный код ответа сервера");

        assertEquals(2, manager.getEpicList().size(),
                "Неверная длина списка эпиков после удаления.");
    }

    /**
     * Тестируем чтение подзадач эпика.
     */
    @Test
    public void testGetSubtasksByEpic()  throws IOException, InterruptedException {
        makeEpicList(3);
        manager.addNewSubtask(new Subtask(2,
                "test Subtask1", "-",
                LocalDateTime.now(),
                Duration.ofMinutes(15)));
        manager.addNewSubtask(new Subtask(2,
                "test Subtask2", "-",
                LocalDateTime.now().plusMinutes(20),
                Duration.ofMinutes(15)));

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/2/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).GET().build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode(),
                "Чтение подзадач эпика: неожиданный код ответа сервера");

        if (response.statusCode() == 200) {
            List<Subtask> subtasksListFromServer = gson.fromJson(response.body(), new SubtaskListTypeToken().getType());

            assertNotNull(subtasksListFromServer,
                    "Список подзадач эпика не прочитан.");

            int expectedSize = manager.getSubtasksByEpic(2).size();
            assertEquals(expectedSize, subtasksListFromServer.size(),
                    "Список подзадач эпика не полный.");
        }
    }

    /**
     * Тестируем ошибку 404
     */
    @Test
    public void testError404() throws IOException, InterruptedException {
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/100");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).GET().build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode(),
                "Чтение несуществующего эпика: неожиданный код ответа сервера");
    }

    /**
     * Создание заданного числа эпиков в менеджере
     *
     * @param size - число эпиков
     */
    private void makeEpicList(int size) {
        if(size == 0) {
            return;
        }
        for (int i = 0; i < size; i ++) {
            manager.addNewEpic(new Epic("epicList element:" + i,
                    "Testing epicList"));
        }
    }

}