package service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private static TaskManager taskManager = Managers.getDefaultTaskManager();
    private static Gson gson = new Gson();

    public static void main(String[] args) throws IOException {

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TaskHandler(taskManager, gson));
        httpServer.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        httpServer.createContext("/epics", new EpicHandler(taskManager, gson));
        httpServer.createContext("/history", new HistoryHandler(taskManager, gson));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));

        httpServer.start();
        System.out.println("Сервер запущен на порту " + PORT);
    }

    static class BaseHttpHandler implements HttpHandler {

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

    static class TaskHandler extends BaseHttpHandler {

        public TaskHandler(TaskManager taskManager, Gson gson) {
            super(taskManager, gson);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                if (path.startsWith("/tasks")) {
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

    static class SubtaskHandler extends BaseHttpHandler {
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

    static class EpicHandler extends BaseHttpHandler {
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

    static class HistoryHandler extends BaseHttpHandler {
        public HistoryHandler(TaskManager taskManager, Gson gson) {
            super(taskManager, gson);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();

                if ("GET".equals(method)) {
                    List<Task> history = taskManager.getHistory();
                    String response = gson.toJson(history);
                    sendText(exchange, response, 200);
                } else {
                    sendText(exchange, "Метод не поддерживается", 405);
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendText(exchange, "Внутренняя ошибка сервера", 500);
            }
        }
    }

    static class PrioritizedHandler extends BaseHttpHandler {
        public PrioritizedHandler(TaskManager taskManager, Gson gson) {
            super(taskManager, gson);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();

                if ("GET".equals(method)) {
                    List<Object> prioritizedTasks = Collections.singletonList(taskManager.getPrioritizedTasks().stream().toList());
                    String response = gson.toJson(prioritizedTasks);
                    sendText(exchange, response, 200);
                } else {
                    sendText(exchange, "Метод не поддерживается", 405);
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendText(exchange, "Внутренняя ошибка сервера", 500);
            }
        }
    }
}
