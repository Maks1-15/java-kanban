package service;

import task.Epic;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaskManager {
    Collection<Task> getAllTask();

    Collection<Epic> getAllEpic();

    Collection<Subtask> getAllSubtask();

    void removeTasks();

    void removeEpics();

    void removeSubtasks();

    Optional<Task> getByIdTask(int id);

    Optional<Epic> getByIdEpic(int id);

    Optional<Subtask> getByIdSubtask(int id);

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

    Optional<Subtask> getSubtaskMaxEndTime();

    Optional<Subtask> getSubtaskMinStartTime();

    public LocalDateTime calculateEpicStartTime();

    public LocalDateTime calculateEpicEndTime();

    Duration getDuration();

    Collection<Task> getPrioritizedTasks();
}

