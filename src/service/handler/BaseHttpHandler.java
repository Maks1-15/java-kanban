package service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler implements HttpHandler {

    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        String response = "Объект не найден";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(404, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    protected void sendHasIntersections(HttpExchange exchange) throws IOException {
        String response = "Задача пересекается с существующими";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(406, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    protected String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected Integer extractIdFromPath(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] pathSegments = path.split("/");
        if (pathSegments.length == 3) {
            try {
                return Integer.parseInt(pathSegments[2]);
            } catch (NumberFormatException e) {
                return null; // Неверный формат ID
            }
        }
        return null; // ID не найден в пути
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }
}
