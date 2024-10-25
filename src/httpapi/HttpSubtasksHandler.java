package httpapi;

import adapters.JsFormatter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import exceptions.TimeIntersectionException;
import managers.TaskManager;
import tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
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
        String endpointName = method + exchange.getRequestURI().getPath();

        try {
            Optional<Integer> subtaskIdOpt = getElementId(exchange);
            if (method.equals("GET")) {
                if (subtaskIdOpt.isEmpty()) {
                    List<Subtask> subtasks = manager.getSubtaskList();
                    sendText(exchange, HttpTaskServer.gson.toJson(subtasks), 200);
                } else {
                    Subtask subtask = manager.getSubtask(subtaskIdOpt.get());
                    sendText(exchange, HttpTaskServer.gson.toJson(subtask), 200);
                }
            } else if (method.equals("POST")) {
                // читаем описание подзадачи переданной в запросе
                InputStream bodyInputStream = exchange.getRequestBody();
                String body = new String(bodyInputStream.readAllBytes(), BaseHttpHandler.DEFAULT_CHARSET);
                Subtask newSubtask = HttpTaskServer.gson.fromJson(body, Subtask.class);

                if (subtaskIdOpt.isEmpty()) {
                    // Добавление новой подзадачи если не указан идентификатор
                    int id = manager.addNewSubtask(newSubtask);
                    if (id > 0) {
                        sendText(exchange,
                                String.format(JsFormatter.ID_MESSAGE, id, "Подзадача успешно добавлена."),
                                201);
                    } else {
                        throw new Exception(endpointName +
                                " Ошибка при добавлении подзадачи. retCode=" + id);
                    }
                } else {
                    // Обновление подзадачи по идентификатору
                    int updateId = subtaskIdOpt.get();
                    newSubtask.setId(updateId);    // на всякий случай устанавлмваем id из параметра запроса
                    manager.updateSubtask(newSubtask);
                    sendText(exchange,
                            String.format(JsFormatter.ID_MESSAGE, updateId, "Подзадача успешно обновлена."),
                            201);
                }
            } else if (method.equals("DELETE")) {
                // Удаление подзадачи
                if (subtaskIdOpt.isEmpty()) {
                    sendText(exchange,
                            String.format(JsFormatter.MESSAGE, "Идентификатор подзадачи не указан."),
                            406);
                } else {
                    int subtaskId = subtaskIdOpt.get();
                    manager.removeSubtask(subtaskId);
                    sendText(exchange,
                            String.format(JsFormatter.ID_MESSAGE, subtaskId, "Задача успешно удалена."),
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
