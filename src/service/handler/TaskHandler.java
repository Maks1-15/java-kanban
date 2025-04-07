package service.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.startsWith("/tasks/task")) {
                switch (method) {
                    case "GET":
                        handleGetTasks(exchange);
                        break;
                    case "POST":
                        handlePostTask(exchange);
                        break;
                    case "DELETE":
                        handleDeleteTask(exchange);
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

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        Integer id = extractIdFromPath(exchange);
        if (id != null) { // GET /tasks/{id}
            Optional<Task> task = taskManager.getByIdTask(id);
            if (task.isPresent()) {
                String response = gson.toJson(task.get());
                sendText(exchange, response, 200);
            } else {
                sendNotFound(exchange);
            }
        } else { // GET /tasks
            List<Task> tasks = taskManager.getAllTask().stream().toList();
            String response = gson.toJson(tasks);
            sendText(exchange, response, 200);
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        try {
            Task task = gson.fromJson(body, Task.class);
            if (task == null) {
                sendText(exchange, "Неверный формат JSON", 400);
                return;
            }

            if (task.getId() == 0) { // Create
                int id = taskManager.createTask(task);
                if (id > 0) {
                    task.setId(id);
                    sendText(exchange, gson.toJson(task), 201);
                } else {
                    sendHasIntersections(exchange); // Или другой код ошибки, если create возвращает ошибку
                }
            } else { // Update
                int id = taskManager.updateTask(task);
                if (id > 0) {
                    sendText(exchange, "Задача обновлена", 201);
                } else {
                    sendNotFound(exchange); // Или другой код ошибки, если update возвращает ошибку
                }
            }

        } catch (JsonSyntaxException e) {
            sendText(exchange, "Неверный формат JSON", 400);
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        Integer id = extractIdFromPath(exchange);
        if (id != null) {
            int result = taskManager.removeTaskById(id);
            if (result > 0) {
                sendText(exchange, "Задача удалена", 201); // Успешное удаление
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendText(exchange, "Не указан ID задачи", 400);
        }
    }
}