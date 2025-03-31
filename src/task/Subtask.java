package task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task  implements Cloneable {

    private int epicId;

    // Конструкторы аналогичны Task
    public Subtask(String name, String description, int epicId) {
        super(name, description, Status.NEW);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, Status status, int epicId, LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, Status status, LocalDateTime startTime, Duration duration, int epicId) {
        super(id, name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public String toFileString() {
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s\n", getId(), "SUBTASK", getName(), getDescription(), getStatus(), getStartTime(), getDuration(), getEpicId());
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() + '\'' +
                ", epicId=' " + getEpicId() + '\'' +
                ", status=" + getStatus() +
                ", duration=" + getDuration() +
                ", startTime=" + getStartTime() +
                '}';

    }

    // Метод копирования задач

    @Override
    public Subtask clone() throws CloneNotSupportedException {
        return (Subtask) super.clone();
    }
}

