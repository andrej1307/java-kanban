package httpapi;

import adapters.JsFormatter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import exceptions.TimeIntersectionException;
import managers.TaskManager;
import tasks.Task;

import java.io.InputStream;
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
    public void handle(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String endpointName = method + exchange.getRequestURI().getPath();

        try {
            Optional<Integer> taskIdOpt = getElementId(exchange);
            if (method.equals("GET")) {
                if (taskIdOpt.isEmpty()) {
                    List<Task> tasks = manager.getTaskList();
                    sendText(exchange, HttpTaskServer.gson.toJson(tasks), 200);
                } else {
                    Task task = manager.getTask(taskIdOpt.get());
                    sendText(exchange, HttpTaskServer.gson.toJson(task), 200);
                }
            } else if (method.equals("POST")) {
                // читаем описание задачи переданной в запросе
                InputStream bodyInputStream = exchange.getRequestBody();
                String body = new String(bodyInputStream.readAllBytes(), BaseHttpHandler.DEFAULT_CHARSET);
                Task newTask = HttpTaskServer.gson.fromJson(body, Task.class);

                if (taskIdOpt.isEmpty()) {
                    // Добавление новой задачи если не указан идентификатор
                    int id = manager.addNewTask(newTask);
                    if (id > 0) {
                        sendText(exchange,
                                String.format(JsFormatter.ID_MESSAGE, id, "Задача успешно добавлена."),
                                201);
                    } else {
                        throw new Exception(endpointName +
                                " Ошибка при добавлении задачи. retCode=" + id);
                    }
                } else {
                    // Обновление задачи по идентификатору
                    int updateId = taskIdOpt.get();
                    newTask.setId(updateId);    // на всякий случай устанавлмваем id из параметра
                    manager.updateTask(newTask);
                    sendText(exchange,
                            String.format(JsFormatter.ID_MESSAGE, updateId, "Задача успешно обновлена."),
                            201);
                }
            } else if (method.equals("DELETE")) {
                // Удаление задачи
                if (taskIdOpt.isEmpty()) {
                    sendText(exchange,
                            String.format(JsFormatter.MESSAGE, "Идентификатор задачи не указан."),
                            406);
                } else {
                    int taskId = taskIdOpt.get();
                    manager.removeTask(taskId);
                    sendText(exchange,
                            String.format(JsFormatter.ID_MESSAGE, taskId, "Задача успешно удалена."),
                            200);
                }
            } else {
                handleUnknown(exchange, endpointName);
            }
        } catch (NotFoundException e) { // Данные не найдены
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, e.getDetailMessage()),
                    404);
        } catch (TimeIntersectionException e) { // Пересечение времени выполнения задач
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, e.getDetailMessage()),
                    406);
        } catch (Exception e) { // Прочие ошибки
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, endpointName + "Ошибка программы.\n" + e.getMessage()),
                    500);
        }
    }
}
