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

import static org.junit.jupiter.api.Assertions.*;

class HttpPrioritizedHandlerTest {
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
    class PrioritizedTypeToken extends TypeToken<List<Task>> {
    }

    /**
     * тестируем получение хронологического списка
     */
    @Test
    public void testPrioritizedList() throws IOException, InterruptedException {
        manager.addNewTask(new Task("Test AddTask1",
                "Testing AddTask",
                LocalDateTime.now(),
                Duration.ofMinutes(15)));

        int epicId = manager.addNewEpic(new Epic("Test subtask epic",
                "Epic for testing Sabtask"));

        manager.addNewSubtask(new Subtask(epicId, "Test AddSubtask2", "-",
                LocalDateTime.now().plusMinutes(30),
                Duration.ofMinutes(15)));

        manager.addNewTask(new Task("Test AddTask2",
                "Testing AddTask2",
                LocalDateTime.now().plusMinutes(80),
                Duration.ofMinutes(15)));

        manager.addNewSubtask(new Subtask(epicId, "Test AddSubtask2", "-",
                LocalDateTime.now().plusMinutes(50),
                Duration.ofMinutes(15)));

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).GET().build();

        // обработчик запроса с конвертацией тела запроса в строку
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode(),
                "Чтение хронологии задач: неожиданный код ответа сервера");

        if (response.statusCode() == 200) {
            List<Task> prioritizedFromServer = gson.fromJson(response.body(),
                    new PrioritizedTypeToken().getType());

            assertNotNull(prioritizedFromServer,
                    "Хронология не прочитана.");

            int expectedSize = manager.getPrioritizedTasks().size();
            assertEquals(expectedSize, prioritizedFromServer.size(),
                    "хронологический cписок не полный.");

            // проверяем хронологическую последовательность полученного списка задач
            for (int i = 0; i < (prioritizedFromServer.size() - 1); i++) {
                assertTrue(prioritizedFromServer.get(i).getEndTime()
                                .isBefore(prioritizedFromServer.get(i+1).getStartTime()),
                        "Нарушена хронология задач");
            }
        }
    }
}