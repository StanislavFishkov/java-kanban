package kanban.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import kanban.server.handlers.*;
import kanban.server.typeadapters.DurationTypeAdapter;
import kanban.server.typeadapters.LocalDateTimeTypeAdapter;
import kanban.service.Managers;
import kanban.service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

public class HttpTaskServer {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final int PORT = 8080;
    private final TaskManager taskManager;
    private HttpServer server;
    protected final Gson gson;

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public HttpServer getServer() {
        return server;
    }

    public Gson getGson() {
        return gson;
    }

    public HttpTaskServer(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .serializeNulls()
                .setPrettyPrinting()
                .create();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHttpHandler(this));
        server.createContext("/subtasks", new SubtasksHttpHandler(this));
        server.createContext("/epics", new EpicsHttpHandler(this));
        server.createContext("/history", new HistoryHttpHandler(this));
        server.createContext("/prioritized", new PrioritizedHttpHandler(this));
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public static void main(String[] args) {
        HttpTaskServer serverInstance = new HttpTaskServer(Managers.getDefault());
        try {
            serverInstance.start();

            System.out.println("Сервер запушен. Нажмите Enter, чтобы его остановить.");
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        } finally {
            serverInstance.stop();
        }
    }
}
