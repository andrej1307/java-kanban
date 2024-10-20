import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;
import adapters.TaskStatusAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import tasks.TaskStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static HttpServer httpServer;
    private static TaskManager manager = Managers.getDefault();

    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
    }

    public Gson getGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(TaskStatus.class, new TaskStatusAdapter())
                .create();
    }

    public static void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new HttpTasksHandler(manager));
        httpServer.createContext("/subtasks", new HttpSubtasksHandler(manager));
        httpServer.createContext("/epics", new HttpEpicsHandler(manager));
        httpServer.createContext("/history", new HttpHistoryHandler(manager));
        httpServer.createContext("/prioritized", new HttpPrioritizedHandler(manager));
        httpServer.start();
    }

    public static void stop() {
        httpServer.stop(0);
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }
}
