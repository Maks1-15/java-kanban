package service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {
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