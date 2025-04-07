package service.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;
import task.Epic;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EpicHandler extends BaseHttpHandler {
    public EpicHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.startsWith("/epics")) {
                switch (method) {
                    case "GET":
                        handleGetEpics(exchange);
                        break;
                    case "POST":
                        handlePostEpic(exchange);
                        break;
                    case "DELETE":
                        handleDeleteEpic(exchange);
                        break;
                    default:
                        sendText(exchange, "Метод не поддерживается", 405);
                }
            } else {
                sendNotFound(exchange);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendText(exchange, "Внутренняя ошибка сервера", 500);
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        Integer id = extractIdFromPath(exchange);
        if (id != null) { // GET /epics/{id}
            Optional<Epic> epic = taskManager.getByIdEpic(id);
            if (epic.isPresent()) {
                String response = gson.toJson(epic.get());
                sendText(exchange, response, 200);
            } else {
                sendNotFound(exchange);
            }
        } else { // GET /epics
            List<Epic> epics = taskManager.getAllEpic().stream().toList();
            String response = gson.toJson(epics);
            sendText(exchange, response, 200);
        }
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        try {
            Epic epic = gson.fromJson(body, Epic.class);
            if (epic == null) {
                sendText(exchange, "Неверный формат JSON", 400);
                return;
            }

            if (epic.getId() == 0) { // Create
                int id = taskManager.createEpic(epic);
                if (id > 0) {
                    epic.setId(id);
                    sendText(exchange, gson.toJson(epic), 201);
                } else {
                    sendHasIntersections(exchange); // Или другой код ошибки, если create возвращает ошибку
                }
            } else { // Update
                int id = taskManager.updateEpic(epic);
                if (id > 0) {
                    sendText(exchange, "Эпик обновлен", 201);
                } else {
                    sendNotFound(exchange); // Или другой код ошибки, если update возвращает ошибку
                }
            }

        } catch (JsonSyntaxException e) {
            sendText(exchange, "Неверный формат JSON", 400);
        }
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        Integer id = extractIdFromPath(exchange);
        if (id != null) {
            int result = taskManager.removeEpicById(id);
            if (result > 0) {
                sendText(exchange, "Эпик удален", 201); // Успешное удаление
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendText(exchange, "Не указан ID эпика", 400);
        }
    }
}

