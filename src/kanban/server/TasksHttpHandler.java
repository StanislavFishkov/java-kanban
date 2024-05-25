package kanban.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.exception.NotFoundException;
import kanban.exception.TasksIntersectedException;
import kanban.model.Task;

import java.io.IOException;

public class TasksHttpHandler extends BaseHttpHandler implements HttpHandler {
    enum Endpoint { GET_TASKS, GET_TASK, POST_TASK, DELETE_TASK, UNKNOWN }

    public TasksHttpHandler(HttpTaskServer taskServer) {
        super(taskServer);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) return Endpoint.GET_TASKS;
            if (requestMethod.equals("POST")) return Endpoint.POST_TASK;
        }
        if (pathParts.length == 3 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) return Endpoint.GET_TASK;
            if (requestMethod.equals("DELETE")) return Endpoint.DELETE_TASK;
        }
        return Endpoint.UNKNOWN;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS -> handleGetTasks(exchange);
            case GET_TASK -> handleGetTask(exchange);
            case POST_TASK -> handlePostTask(exchange);
            case DELETE_TASK -> handleDeleteTask(exchange);
            default -> sendNotFound(exchange);
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        sendText(exchange, taskServer.getGson().toJson(taskServer.getTaskManager().getTasks()));
    }

    private void handleGetTask(HttpExchange exchange) throws IOException {
        Task task;
        try {
            int taskId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            task = taskServer.getTaskManager().getTask(taskId);
        } catch (NumberFormatException | NotFoundException exception) {
            sendNotFound(exchange);
            return;
        }

        sendText(exchange, taskServer.getGson().toJson(task));
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), HttpTaskServer.DEFAULT_CHARSET);
            Task task = taskServer.getGson().fromJson(body, Task.class);
            if (task.getId() == null) {
                taskServer.getTaskManager().addTask(task);
            } else {
                taskServer.getTaskManager().updateTask(task);
            }
        } catch (NotFoundException exception) {
            sendNotFound(exchange);
            return;
        } catch (TasksIntersectedException exception) {
            sendHasInteractions(exchange);
            return;
        } catch (Exception exception) {
            sendError(exchange);
            return;
        }

        sendCreated(exchange);
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        try {
            int taskId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            taskServer.getTaskManager().deleteTask(taskId);
        } catch (NumberFormatException | NotFoundException exception) {
            sendNotFound(exchange);
            return;
        } catch (Exception exception) {
            sendError(exchange);
            return;
        }

        sendSuccess(exchange);
    }
}