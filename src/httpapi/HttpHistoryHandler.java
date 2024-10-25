package httpapi;

import adapters.JsFormatter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import managers.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.util.List;

public class HttpHistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;

    public HttpHistoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String endpointName = method + exchange.getRequestURI().getPath();

        try {
            if (!method.equals("GET")) {
                handleUnknown(exchange, endpointName);
                return;
            }

            List<Task> history = manager.getHistory();
            sendText(exchange, HttpTaskServer.gson.toJson(history), 200);

        } catch (NotFoundException e) { // Данные не найдены
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, e.getDetailMessage()),
                    404);
        } catch (Exception e) { // Прочие ошибки
            String.format(JsFormatter.MESSAGE, endpointName +
                            "Ошибка программы.\n" + e.getMessage(),
                    500);
        }
    }
}
