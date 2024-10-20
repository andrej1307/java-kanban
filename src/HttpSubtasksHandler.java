import adapters.DurationAdapter;
import adapters.JsFormatter;
import adapters.LocalDateTimeAdapter;
import adapters.TaskStatusAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.TaskCrossTimeException;
import tasks.Subtask;
import tasks.TaskStatus;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class HttpSubtasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;

    public HttpSubtasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET" -> handleSubtasksGet(exchange);
            case "POST" -> handleSubtaskPost(exchange);
            case "DELETE" -> handleSubtaskDelete(exchange);
            default -> handleUnknown(exchange, method);
        }
    }

    /**
     * Обработка запросов GET
     *
     * @param exchange
     * @throws IOException
     */
    private void handleSubtasksGet(HttpExchange exchange) throws IOException {
        Gson gson = getGson();

        Optional<Integer> subtaskIdOpt = getElementId(exchange);

        if (subtaskIdOpt.isEmpty()) {
            List<Subtask> subtasks = manager.getSubtaskList();
            if (subtasks == null || subtasks.isEmpty()) {
                sendText(exchange,
                        String.format(JsFormatter.MESSAGE, "Задачи не найдены."),
                        404);
            } else { // передаем в ответе информацию о списке задач
                sendText(exchange, gson.toJson(subtasks), 200);
            }
        } else {
            int subtaskId = subtaskIdOpt.get();
            Subtask subtask = manager.getSubtask(subtaskId);
            if (subtask == null) {
                sendText(exchange,
                        String.format(JsFormatter.ID_MESSAGE, subtaskId, "Подзадача не найдена."),
                        404);
            } else { // передаем в ответе информацию о подзадаче
                sendText(exchange, gson.toJson(subtask), 200);
            }
        }
    }

    /**
     * Обработка запросов POST
     *
     * @param exchange
     * @throws IOException
     */
    private void handleSubtaskPost(HttpExchange exchange) throws IOException {
        Gson gson = getGson();
        InputStream bodyInputStream = exchange.getRequestBody();
        String body = new String(bodyInputStream.readAllBytes(), DEFAULT_CHARSET);

        Subtask newSubtask = null;
        try {
            newSubtask = gson.fromJson(body, Subtask.class);
        } catch (Exception e) { // ошибка при десиарилизации объекта
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, e.getMessage()),
                    400);
        }
        if (newSubtask == null) {
            return;
        }

        Integer epicId = newSubtask.getEpicId();
        if (epicId == null || manager.getEpic(epicId) == null) {
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, "У подзадачи указан некорректный идентификатор эпика."),
                    406);
            return;
        }
        if (newSubtask.getStartTime() == null) {
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, "Не указан обязательный параметр: startTime."),
                    406);
            return;
        }

        Optional<Integer> subtaskIdOpt = getElementId(exchange);
        if (subtaskIdOpt.isEmpty()) { // Добавление новой подзадачи
            try {
                int id = manager.addNewSubtask(newSubtask);
                if (id > 0) {
                    sendText(exchange,
                            String.format(JsFormatter.ID_MESSAGE, id, "Подзадача успешно добавлена."),
                            201);
                }
                sendText(exchange,
                        String.format(JsFormatter.MESSAGE, "Произошла внутренняя ошибка."),
                        500);

            } catch (TaskCrossTimeException e) { // обрботка исключения по времени
                sendText(exchange,
                        String.format(JsFormatter.MESSAGE, e.getDetailMessage()),
                        406);
            }
        } else { // Обновление подзадачи по указанному идентификатору
            int updateId = subtaskIdOpt.get();
            newSubtask.setId(updateId);    // на всякий случай устанавлмваем id из параметра
            if (updateId < 1) {
                sendText(exchange,
                        String.format(JsFormatter.ID_MESSAGE, updateId, "Недопустимый идентификатор."),
                        406);
            } else {
                try {
                    manager.updateSubtask(newSubtask);
                    sendText(exchange,
                            String.format(JsFormatter.ID_MESSAGE, updateId, "Подзадача успешно обновлена."),
                            201);
                } catch (TaskCrossTimeException e) {
                    sendText(exchange,
                            String.format(JsFormatter.MESSAGE, e.getDetailMessage()),
                            406);
                }
            }
        }
    }

    /**
     * Обработка DELETE
     *
     * @param exchange
     * @throws IOException
     */
    private void handleSubtaskDelete(HttpExchange exchange) throws IOException {
        Optional<Integer> subtaskIdOpt = getElementId(exchange);
        if (subtaskIdOpt.isEmpty()) { // Удаление задачи
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, "Идентификатор подзадачи не указан."),
                    406);
        } else {
            int subtaskId = subtaskIdOpt.get();
            if (manager.getSubtask(subtaskId) == null) {
                sendText(exchange,
                        String.format(JsFormatter.ID_MESSAGE, subtaskId, "Подзадача не найдена."),
                        404);
            } else {
                manager.removeSubtask(subtaskId);
                sendText(exchange,
                        String.format(JsFormatter.ID_MESSAGE, subtaskId, "Подзадача успешно удалена."),
                        200);
            }
        }
    }

}
