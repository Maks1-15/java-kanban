package task;

import service.Managers;
import service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    TaskManager taskManager = Managers.getDefaultTaskManager();

    private final List<Integer> epicSubtaskId;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.epicSubtaskId = new ArrayList<>();
    }

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, Status.NEW);
        this.epicSubtaskId = new ArrayList<>();
    }

    public List<Integer> getEpicSubtask() {
        return epicSubtaskId;
    }

    public void addEpicSubtask(int id) {
        this.epicSubtaskId.add(id);
    }

    public void removeSubtaskById(Integer subtaskId) {
        this.epicSubtaskId.remove(subtaskId);
    }

    public String toFileString() {
        return String.format("%d,%s,%s,%s,%s,\n", getId(), "EPIC", getName(), getDescription(), getStatus());
    }

    // переопределяем геттеры для получения значений из TaskManager, т.к. логика расчета оттуда


    @Override
    public LocalDateTime getStartTime() {
        return taskManager.calculateEpicStartTime();
    }

    @Override
    public LocalDateTime getEndTime() {
        return taskManager.calculateEpicEndTime();
    }

    @Override
    public Duration getDuration() {
        return taskManager.getDuration();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() + '\'' +
                ", listSubtask=' " + getEpicSubtask() + '\'' +
                ", status=" + getStatus() +
//                ", duration=" + getDuration() +
//                ", startTime=" + getStartTime() +
                '}';
    }
}
