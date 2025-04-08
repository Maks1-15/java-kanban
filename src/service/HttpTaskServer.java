package service;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import service.handler.EpicHandler;
import service.handler.HistoryHandler;
import service.handler.SubtaskHandler;
import service.handler.TaskHandler;
import service.utils.DurationAdapter;
import service.utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {

    private final int port;
    private TaskManager taskManager = Managers.getDefaultTaskManager();
    private HttpServer httpServer;
    private Gson gson;

    public HttpTaskServer(int port, TaskManager taskManager) {
        this.port = port;
        this.taskManager = taskManager;
        gson = Managers.getGson();
    }

    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/tasks/task", new TaskHandler(taskManager, gson));
        httpServer.createContext("/tasks/epic", new EpicHandler(taskManager, gson));
        httpServer.createContext("/tasks/subtask", new SubtaskHandler(taskManager, gson));
        httpServer.createContext("/tasks/history", new HistoryHandler(taskManager, gson));
        httpServer.start();
        System.out.println("Сервер запущен на порту: " + port);
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0); // 0 означает немедленную остановку
            System.out.println("HTTP task server stopped.");
        }
    }

    public static void main(String[] args) throws IOException {

        int port = 8080;
        TaskManager taskManager = new InMemoryTaskManager();
        HttpTaskServer server = new HttpTaskServer(port, taskManager);
        server.start();

        // Пример остановки сервера через некоторое время (для демонстрации)
//        try {
//            Thread.sleep(30000); // Ждем 30 секунд
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        server.stop();
    }
}
