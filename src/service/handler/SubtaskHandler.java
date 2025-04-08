package service.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;
import task.Subtask;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class SubtaskHandler extends BaseHttpHandler {
    public SubtaskHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.startsWith("/subtasks")) {
                switch (method) {
                    case "GET":
                        handleGetSubtasks(exchange);
                        break;
                    case "POST":
                        handlePostSubtask(exchange);
                        break;
                    case "DELETE":
                        handleDeleteSubtask(exchange);
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

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        Integer id = extractIdFromPath(exchange);
        if (id != null) { // GET /subtasks/{id}
            Optional<Subtask> subtask = taskManager.getByIdSubtask(id);
            if (subtask.isPresent()) {
                String response = gson.toJson(subtask.get());
                sendText(exchange, response, 200);
            } else {
                sendNotFound(exchange);
            }
        } else { // GET /subtasks
            List<Subtask> subtasks = taskManager.getAllSubtask().stream().toList();
            String response = gson.toJson(subtasks);
            sendText(exchange, response, 200);
        }
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        try {
            Subtask subtask = gson.fromJson(body, Subtask.class);
            if (subtask == null) {
                sendText(exchange, "Неверный формат JSON", 400);
                return;
            }

            if (subtask.getId() == 0) { // Create
                int id = taskManager.createSubtask(subtask);
                if (id > 0) {
                    subtask.setId(id);
                    sendText(exchange, gson.toJson(subtask), 201);
                } else {
                    sendHasIntersections(exchange); // Или другой код ошибки, если create возвращает ошибку
                }
            } else { // Update
                int id = taskManager.updateSubtask(subtask);
                if (id > 0) {
                    sendText(exchange, "Подзадача обновлена", 201);
                } else {
                    sendNotFound(exchange); // Или другой код ошибки, если update возвращает ошибку
                }
            }

        } catch (JsonSyntaxException e) {
            sendText(exchange, "Неверный формат JSON", 400);
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        Integer id = extractIdFromPath(exchange);
        if (id != null) {
            int result = taskManager.removeSubtaskById(id);
            if (result > 0) {
                sendText(exchange, "Подзадача удалена", 201); // Успешное удаление
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendText(exchange, "Не указан ID подзадачи", 400);
        }
    }
}

