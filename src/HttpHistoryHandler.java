import adapters.JsFormatter;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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

        if (!method.equals("GET")) {
            handleUnknown(exchange, method);
            return;
        }

        Gson gson = getGson();

        List<Task> history = manager.getHistory();
        try {
            sendText(exchange, gson.toJson(history), 200);
        } catch (Exception e) {
            sendText(exchange,
                    String.format(JsFormatter.MESSAGE, e.getMessage()),
                    500);
        }
    }
}
