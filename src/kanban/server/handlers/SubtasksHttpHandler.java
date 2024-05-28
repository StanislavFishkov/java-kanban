package kanban.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.exception.NotFoundException;
import kanban.exception.TasksIntersectedException;
import kanban.model.Subtask;
import kanban.server.HttpTaskServer;

import java.io.IOException;

public class SubtasksHttpHandler extends BaseHttpHandler implements HttpHandler {
    enum Endpoint { GET_SUBTASKS, GET_SUBTASK, POST_SUBTASK, DELETE_SUBTASK, UNKNOWN }

    public SubtasksHttpHandler(HttpTaskServer taskServer) {
        super(taskServer);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts[1].equals("subtasks")) {
            if (pathParts.length == 2) {
                if (requestMethod.equals("GET")) return Endpoint.GET_SUBTASKS;
                if (requestMethod.equals("POST")) return Endpoint.POST_SUBTASK;
            }
            if (pathParts.length == 3) {
                if (requestMethod.equals("GET")) return Endpoint.GET_SUBTASK;
                if (requestMethod.equals("DELETE")) return Endpoint.DELETE_SUBTASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_SUBTASKS -> handleGetSubtasks(exchange);
            case GET_SUBTASK -> handleGetSubtask(exchange);
            case POST_SUBTASK -> handlePostSubtask(exchange);
            case DELETE_SUBTASK -> handleDeleteSubtask(exchange);
            default -> sendNotFound(exchange);
        }
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        sendText(exchange, taskServer.getGson().toJson(taskServer.getTaskManager().getSubtasks()));
    }

    private void handleGetSubtask(HttpExchange exchange) throws IOException {
        Subtask subtask;
        try {
            int subtaskId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            subtask = taskServer.getTaskManager().getSubtask(subtaskId);
        } catch (NumberFormatException | NotFoundException exception) {
            sendNotFound(exchange);
            return;
        }

        sendText(exchange, taskServer.getGson().toJson(subtask));
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), HttpTaskServer.DEFAULT_CHARSET);
            Subtask subtask = taskServer.getGson().fromJson(body, Subtask.class);
            if (subtask.getId() == null) {
                taskServer.getTaskManager().addSubtask(subtask);
            } else {
                taskServer.getTaskManager().updateSubtask(subtask);
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

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        try {
            int subtaskId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            taskServer.getTaskManager().deleteSubtask(subtaskId);
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