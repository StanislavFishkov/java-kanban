package kanban.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.exception.NotFoundException;
import kanban.exception.TasksIntersectedException;
import kanban.model.Epic;

import java.io.IOException;

public class EpicsHttpHandler extends BaseHttpHandler implements HttpHandler {
    enum Endpoint { GET_EPICS, GET_EPIC, POST_EPIC, DELETE_EPIC, GET_EPIC_SUBTASKS, UNKNOWN }

    public EpicsHttpHandler(HttpTaskServer taskServer) {
        super(taskServer);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts[1].equals("epics")) {
            if (pathParts.length == 2) {
                if (requestMethod.equals("GET")) return Endpoint.GET_EPICS;
                if (requestMethod.equals("POST")) return Endpoint.POST_EPIC;
            }
            if (pathParts.length == 3) {
                if (requestMethod.equals("GET")) return Endpoint.GET_EPIC;
                if (requestMethod.equals("DELETE")) return Endpoint.DELETE_EPIC;
            }
            if (pathParts.length == 4 && pathParts[3].equals("subtasks")) {
                if (requestMethod.equals("GET")) return Endpoint.GET_EPIC_SUBTASKS;
            }
        }
        return Endpoint.UNKNOWN;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_EPICS -> handleGetEpics(exchange);
            case GET_EPIC -> handleGetEpic(exchange);
            case POST_EPIC -> handlePostEpic(exchange);
            case DELETE_EPIC -> handleDeleteEpic(exchange);
            case GET_EPIC_SUBTASKS -> handleGetEpicSubtasks(exchange);
            default -> sendNotFound(exchange);
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        sendText(exchange, taskServer.getGson().toJson(taskServer.getTaskManager().getEpics()));
    }

    private void handleGetEpic(HttpExchange exchange) throws IOException {
        Epic epic;
        try {
            int epicId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            epic = taskServer.getTaskManager().getEpic(epicId);
        } catch (NumberFormatException | NotFoundException exception) {
            sendNotFound(exchange);
            return;
        }

        sendText(exchange, taskServer.getGson().toJson(epic));
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        Epic epic;
        try {
            int epicId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            epic = taskServer.getTaskManager().getEpic(epicId);
        } catch (NumberFormatException | NotFoundException exception) {
            sendNotFound(exchange);
            return;
        }

        sendText(exchange, taskServer.getGson().toJson(taskServer.getTaskManager().getEpicSubtasks(epic)));
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), HttpTaskServer.DEFAULT_CHARSET);
            Epic epic = taskServer.getGson().fromJson(body, Epic.class);
            if (epic.getId() == null) {
                taskServer.getTaskManager().addEpic(epic);
            } else {
                taskServer.getTaskManager().updateEpic(epic);
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

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        try {
            int epicId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            taskServer.getTaskManager().deleteEpic(epicId);
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