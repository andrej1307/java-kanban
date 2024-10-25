package httpapi;

import adapters.JsFormatter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import exceptions.TimeIntersectionException;
import managers.TaskManager;
import tasks.Epic;
import tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * класс обработки http запросов по контексту /epics
 */
public class HttpEpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;

    public HttpEpicsHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String endpointName = method + exchange.getRequestURI().getPath();

        try {
            Optional<Integer> epicIdOpt = getElementId(exchange);
            if (method.equals("GET")) {
                if (epicIdOpt.isEmpty()) {
                    List<Epic> epics = manager.getEpicList();
                    sendText(exchange, HttpTaskServer.gson.toJson(epics), 200);
                } else {
                    int epicId = epicIdOpt.get();
                    Epic epic = manager.getEpic(epicId);

                    String path = exchange.getRequestURI().getPath();
                    if (!path.contains("subtasks")) {
                        sendText(exchange, HttpTaskServer.gson.toJson(epic), 200);
                    } else {
                        List<Subtask> subtasks = manager.getSubtasksByEpic(epicId);
                        sendText(exchange, HttpTaskServer.gson.toJson(subtasks), 200);
                    }
                }
            } else if (method.equals("POST")) {
                // читаем описание эпика переданной в запросе
                InputStream bodyInputStream = exchange.getRequestBody();
                String body = new String(bodyInputStream.readAllBytes(), BaseHttpHandler.DEFAULT_CHARSET);
                Epic newEpic = HttpTaskServer.gson.fromJson(body, Epic.class);

                if (epicIdOpt.isEmpty()) {
                    // Добавление нового эпика если не указан идентификатор
                    int id = manager.addNewEpic(newEpic);
                    if (id > 0) {
                        sendText(exchange,
                                String.format(JsFormatter.ID_MESSAGE, id, "Эпик успешно добавлена."),
                                201);
                    } else {
                        throw new Exception(endpointName +
                                " Ошибка при добавлении эпика. retCode=" + id);
                    }
                } else {
                    // Обновление эпика по идентификатору
                    int updateId = epicIdOpt.get();
                    newEpic.setId(updateId);    // на всякий случай устанавлмваем id из параметра
                    manager.updateEpic(newEpic);
                    sendText(exchange,
                            String.format(JsFormatter.ID_MESSAGE, updateId, "Задача успешно обновлена."),
                            201);
                }
            } else if (method.equals("DELETE")) {
                // Удаление задачи
                if (epicIdOpt.isEmpty()) {
                    sendText(exchange,
                            String.format(JsFormatter.MESSAGE, "Идентификатор эпика не указан."),
                            406);
                } else {
                    int epicId = epicIdOpt.get();
                    manager.removeEpic(epicId);
                    sendText(exchange,
                            String.format(JsFormatter.ID_MESSAGE, epicId, "эпик успешно удален."),
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
