package service;

import task.Task;
import java.io.File;
import java.util.List;

// Вспомогательный класс для работы с приложением

public final class Managers {

    TaskManager tm = new InMemoryTaskManager();

    public List<Task> getDefaultHistory() {
        return tm.getHistory();
    }

    // Дефолтный TaskManager
    public static TaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager();
    }

    // Дефолтный HistoryManager
    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }

    // Дефолтный FileBackedTaskManager
    public static TaskManager getFileBackedTaskManager(File file) {
        return new FileBackedTaskManager(file);
    }

}
