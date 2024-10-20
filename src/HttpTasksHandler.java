import adapters.DurationAdapter;
import adapters.JsFormatter;
import adapters.LocalDateTimeAdapter;
import adapters.TaskStatusAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.TaskCrossTimeException;
import tasks.Task;
import tasks.TaskStatus;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * класс обработки http запросов по контексту /tasks
 */
public class HttpTasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;

    public HttpTasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET" -> handleTasksGet(exchange);
            case "POST" -> handleTaskPost(exchange);
            case "DELETE" -> handleTaskDelete(exchange);
            default -> handleUnknown(exchange, method);
        }
    }

    /**
     * Обработка запросов GET
     *
     * @param exchange - объект HTTP запроса
     * @throws IOException - исключение для обработки ошибок ввода, вывода
     */
    private void handleTasksGet(HttpExchange exchange) throws IOException {
        Gson gson = getGson();

        Optional<Integer> taskIdOpt = getElementId(exchange);

        if (taskIdOpt.isEmpty()) {
            List<Task> tasks = manager.getTaskList();
            if (tasks == null || tasks.isEmpty()) {
                sendText(exchange,
                        String.format(JsFormatter.MESSAGE, "Задачи не найдены."),
                        404);
            } else { // передаем в ответе информацию о списке задач
                sendText(exchange, gson.toJson(tasks), 200);
            }
        } else {
            int taskId = taskIdOpt.get();
            Task task = manager.getTask(taskId);
            if (task == null) {
                sendText(exchange,
                        String.format(JsFormatter.ID_MESSAGE, taskId, "Задача не найдена."),
                        404);
            } else { // передаем в ответе информацию о задаче
                sendText(exchange, gson.toJson(task), 200);
            }
        }
    }

    /**
     * Обработка запросов POST
     *
     * @param exchange
     * @throws IOException
     */
    private void handleTaskPost(HttpExchange exchange)
            throws IOException {
        Gson gson = getGson();
        InputStream bodyInputStream = exchange.getRequestBody();
        String body = new String(bodyInputStream.readAllBytes(), DEFAULT_CHARSET);
        Task newTask = null;

        try {
            newTask = gson.fromJson(body, Task.class);
        } catch (Exception e) { // ошибка при десиарилизации объекта
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, e.getMessage()),
                    400);
        }
        if (newTask == null) {
            return;
        }

        if (newTask.getStartTime() == null) {
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, "Не указан обязательный параметр: startTime."),
                    406);
            return;
        }

        Optional<Integer> taskIdOpt = getElementId(exchange);
        if (taskIdOpt.isEmpty()) { // Добавление новой задачи
            try {
                int id = manager.addNewTask(newTask);
                if (id > 0) {
                    sendText(exchange,
                            String.format(JsFormatter.ID_MESSAGE, id, "Задача успешно добавлена."),
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
        } else { // Обновление задачи по указанному идентификатору
            int updateId = taskIdOpt.get();
            newTask.setId(updateId);    // на всякий случай устанавлмваем id из параметра
            if (updateId < 1) {
                sendText(exchange,
                        String.format(JsFormatter.ID_MESSAGE, updateId, "Недопустимый идентификатор."),
                        406);
            } else {
                try {
                    manager.updateTask(newTask);
                    sendText(exchange,
                            String.format(JsFormatter.ID_MESSAGE, updateId, "Задача успешно обновлена."),
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
    private void handleTaskDelete(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getElementId(exchange);
        if (taskIdOpt.isEmpty()) { // Удаление задачи
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, "Идентификатор задачи не указан."),
                    406);
        } else {
            int taskId = taskIdOpt.get();
            if (manager.getTask(taskId) == null) {
                sendText(exchange,
                        String.format(JsFormatter.ID_MESSAGE, taskId, "Задача не найдена."),
                        404);
            } else {
                manager.removeTask(taskId);
                sendText(exchange,
                        String.format(JsFormatter.ID_MESSAGE, taskId, "Задача успешно удалена."),
                        200);
            }
        }
    }

}
