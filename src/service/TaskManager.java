package service;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.Collection;
import java.util.List;

public interface TaskManager {
    Collection<Task> getAllTask();

    Collection<Epic> getAllEpic();

    Collection<Subtask> getAllSubtask();

    void removeTasks();

    void removeEpics();

    void removeSubtasks();

    Task getByIdTask(int id);

    Epic getByIdEpic(int id);

    Subtask getByIdSubtask(int id);

    int createTask(Task task);

    int createEpic(Epic epic);

    int createSubtask(Subtask subtask);

    int updateTask(Task task);

    int updateEpic(Epic epic);

    int updateSubtask(Subtask subtask);

    int removeTaskById(int id);

    int removeEpicById(int id);

    int removeSubtaskById(int id);

    // Возврат Subtask по Epic
    List<Subtask> getEpicSubtask(Epic epic);

    void updateEpicStatus(int epicId);

    public List<Task> getHistory();
}
