package kanban.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.server.HttpTaskServer;

import java.io.IOException;

public class HistoryHttpHandler extends BaseHttpHandler implements HttpHandler {
    enum Endpoint { GET_HISTORY, UNKNOWN }

    public HistoryHttpHandler(HttpTaskServer taskServer) {
        super(taskServer);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts[1].equals("history")) {
            if (pathParts.length == 2) {
                if (requestMethod.equals("GET")) return Endpoint.GET_HISTORY;
            }
        }
        return Endpoint.UNKNOWN;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_HISTORY -> handleGetHistory(exchange);
            default -> sendNotFound(exchange);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        sendText(exchange, taskServer.getGson().toJson(taskServer.getTaskManager().getHistory()));
    }
}

