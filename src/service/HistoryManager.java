package service;

import task.Task;

import java.util.List;

public interface HistoryManager {

    int addTaskInMapHistory(Task task);

    void removeIdByHistoryMap(int id);

    List<Task> getHistory();
}
