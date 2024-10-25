package httpapi;

import adapters.JsFormatter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class BaseHttpHandler {
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Передач ответа на HTTP запрос
     *
     * @param h-      объект http запроса
     * @param text    - текст ответа
     * @param retCode - код завершения обработки запроса
     */
    public void sendText(HttpExchange h, String text, int retCode) /*throws IOException*/ {
        try {
            byte[] resp = text.getBytes(StandardCharsets.UTF_8);
            h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            h.sendResponseHeaders(retCode, resp.length);
            h.getResponseBody().write(resp);
        } catch (IOException e) {
            System.out.println("Произошла ошибка при передаче ответа на запрос.\n" +
                    e.getMessage());
        }
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
     */
    protected void handleUnknown(HttpExchange exchange, String method) {
        sendText(exchange,
                String.format(JsFormatter.MESSAGE, "Метод не поддержмвается - " + method),
                406);
    }

}