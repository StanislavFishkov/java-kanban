package kanban.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.server.HttpTaskServer;

import java.io.IOException;

public class PrioritizedHttpHandler extends BaseHttpHandler implements HttpHandler {
    enum Endpoint { GET_PRIORITIZED, UNKNOWN }

    public PrioritizedHttpHandler(HttpTaskServer taskServer) {
        super(taskServer);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts[1].equals("prioritized")) {
            if (pathParts.length == 2) {
                if (requestMethod.equals("GET")) return Endpoint.GET_PRIORITIZED;
            }
        }
        return Endpoint.UNKNOWN;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_PRIORITIZED -> handleGetPrioritized(exchange);
            default -> sendNotFound(exchange);
        }
    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        sendText(exchange, taskServer.getGson().toJson(taskServer.getTaskManager().getPrioritizedTasks()));
    }
}
