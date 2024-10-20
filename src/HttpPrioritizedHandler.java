import adapters.JsFormatter;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tasks.Task;

import java.io.IOException;
import java.util.List;

public class HttpPrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;

    public HttpPrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if (!method.equals("GET")) {
            handleUnknown(exchange, method);
            return;
        }

        Gson gson = getGson();

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        try {
            sendText(exchange, gson.toJson(prioritizedTasks), 200);
        } catch (Exception e) {
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, e.getMessage()),
                    500);
        }
    }

}
