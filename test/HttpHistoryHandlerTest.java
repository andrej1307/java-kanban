import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpHistoryHandlerTest {
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
     * Вспомогательный класс определение типа объекта десериализации
     */
    class HistoryListTypeToken extends TypeToken<List<Task>> {
    }

    /**
     * Тестируем получение истории оюращения к задачам
     */
    @Test
    public void testHistory() throws IOException, InterruptedException {
        int taskId = manager.addNewTask(new Task("Test AddTask",
                "Testing AddTask",
                LocalDateTime.now(),
                Duration.ofMinutes(15)));

        int epicId = manager.addNewEpic(new Epic("Test subtask epic",
                "Epic for testing Sabtask"));

        int subtaskId = manager.addNewSubtask(new Subtask(epicId, "Test AddSubtask", "-",
                LocalDateTime.now().plusMinutes(30),
                Duration.ofMinutes(15)));

        Task task = manager.getTask(taskId);
        Epic epic = manager.getEpic(epicId);
        Subtask subtask = manager.getSubtask(subtaskId);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).GET().build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode(),
                "Чтение истории задач: неожиданный код ответа сервера");

        if (response.statusCode() == 200) {
            List<Task> historyFromServer = gson.fromJson(response.body(),
                    new HistoryListTypeToken().getType());

            assertNotNull(historyFromServer,
                    "история не прочитана.");

            int expectedSize = manager.getHistory().size();
            assertEquals(expectedSize, historyFromServer.size(),
                    "Список истории не полный.");
        }
    }
}