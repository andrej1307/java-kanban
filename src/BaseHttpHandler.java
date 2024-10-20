import adapters.DurationAdapter;
import adapters.JsFormatter;
import adapters.LocalDateTimeAdapter;
import adapters.TaskStatusAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import tasks.TaskStatus;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class BaseHttpHandler {
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public void sendText(HttpExchange h, String text, int retCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(retCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    /**
     * Настройка параметров сериализации объектов JSON
     *
     * @return - объект конвертера gson
     */
    public Gson getGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(TaskStatus.class, new TaskStatusAdapter())
                .create();
    }

    /**
     * Чтение параметра http запроса
     *
     * @param h - объект http запроса
     * @return - идентификатор элемента в списке задач
     */
    public Optional<Integer> getElementId(HttpExchange h) {
        String[] pathParts = h.getRequestURI().getPath().split("/");
        if (pathParts.length < 3) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    /**
     * Обработка Нераспознанного запроса
     *
     * @param exchange
     * @throws IOException
     */
    protected void handleUnknown(HttpExchange exchange, String method) throws IOException {
        sendText(exchange,
                String.format(JsFormatter.MESSAGE, "Метод не поддержмвается : " + method),
                406);
    }

}