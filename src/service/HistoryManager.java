package service;

import task.Task;

import java.util.List;

public interface HistoryManager {

    int addTaskInMapHistory(Task task);

    int removeIdByHistoryMap(int id);

    List<Task> getHistory();
}
