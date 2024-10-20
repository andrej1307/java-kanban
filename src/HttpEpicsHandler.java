import adapters.JsFormatter;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.TaskCrossTimeException;
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
        switch (method) {
            case "GET" -> handleEpicsGet(exchange);
            case "POST" -> handleEpicsPost(exchange);
            case "DELETE" -> handleEpicsDelete(exchange);
            default -> handleUnknown(exchange, method);
        }
    }

    /**
     * Обработка запросов GET
     *
     * @param exchange - объект HTTP запроса
     * @throws IOException - исключение для обработки ошибок ввода, вывода
     */
    private void handleEpicsGet(HttpExchange exchange) throws IOException {
        Gson gson = getGson();

        Optional<Integer> epicIdOpt = getElementId(exchange);

        if (epicIdOpt.isEmpty()) {
            List<Epic> epics = manager.getEpicList();
            if (epics == null || epics.isEmpty()) {
                sendText(exchange,
                        String.format(JsFormatter.MESSAGE, "Эпики не найдены."),
                        404);
            } else { // передаем в ответе информацию о списке эпиков
                sendText(exchange, gson.toJson(epics), 200);
            }
        } else {
            int epicId = epicIdOpt.get();
            Epic epic = manager.getEpic(epicId);
            if (epic == null) {
                sendText(exchange,
                        String.format(JsFormatter.ID_MESSAGE, epicId, "Эпик не найден."),
                        404);
            } else {
                String path = exchange.getRequestURI().getPath();
                if (!path.contains("subtasks")) {
                    // передаем в ответе информацию об эпике
                    sendText(exchange, gson.toJson(epic), 200);
                } else {
                    List<Subtask> subtasks = manager.getSubtasksByEpic(epicId);
                    if (subtasks.isEmpty()) {
                        sendText(exchange,
                                String.format(JsFormatter.MESSAGE, "Подзадачи не найдены."),
                                404);
                    } else {
                        // передаем в ответе информацию о подзадачах эпика.
                        sendText(exchange, gson.toJson(subtasks), 200);
                    }
                }
            }
        }
    }

    /**
     * Обработка запросов POST
     *
     * @param exchange - объект HTTP запроса
     * @throws IOException - исключение для обработки ошибок ввода, вывода
     */
    private void handleEpicsPost(HttpExchange exchange) throws IOException {
        Gson gson = getGson();
        InputStream bodyInputStream = exchange.getRequestBody();
        String body = new String(bodyInputStream.readAllBytes(), DEFAULT_CHARSET);

        Epic newEpic = null;
        try {
            newEpic = gson.fromJson(body, Epic.class);
        } catch (Exception e) { // ошибка при десиарилизации объекта
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, e.getMessage()),
                    400);
        }
        if (newEpic == null) {
            return;
        }

        Optional<Integer> epicIdOpt = getElementId(exchange);
        if (epicIdOpt.isEmpty()) { // Добавление нового Эпика
            try {
                int id = manager.addNewEpic(newEpic);
                if (id > 0) {
                    sendText(exchange,
                            String.format(JsFormatter.ID_MESSAGE, id, "Эпик успешно добавлен."),
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
        } else { // Обновление эпика по указанному идентификатору
            int updateId = epicIdOpt.get();
            newEpic.setId(updateId);    // на всякий случай устанавлмваем id из параметра
            if (updateId < 1) {
                sendText(exchange,
                        String.format(JsFormatter.ID_MESSAGE, updateId, "Недопустимый идентификатор."),
                        406);
            } else {
                try {
                    manager.updateEpic(newEpic);
                    sendText(exchange,
                            String.format(JsFormatter.ID_MESSAGE, updateId, "Эпик успешно обновлен."),
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
     * @param exchange - объект HTTP запроса
     * @throws IOException - исключение для обработки ошибок ввода, вывода
     */
    private void handleEpicsDelete(HttpExchange exchange) throws IOException {
        Optional<Integer> epicIdOpt = getElementId(exchange);
        if (epicIdOpt.isEmpty()) { // Удаление задачи
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, "Идентификатор эпика не указан."),
                    406);
        } else {
            int epicId = epicIdOpt.get();
            if (manager.getEpic(epicId) == null) {
                sendText(exchange,
                        String.format(JsFormatter.ID_MESSAGE, epicId, "Эпик не найден."),
                        404);
            } else {
                manager.removeEpic(epicId);
                sendText(exchange,
                        String.format(JsFormatter.ID_MESSAGE, epicId, "Эпик успешно удален."),
                        200);
            }
        }
    }

}
