import task.*;
import service.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager imtm = (InMemoryTaskManager) Managers.getDefaultTaskManager();

        Task task1 = new Task("task1", "des", Status.NEW, LocalDateTime.now(), Duration.ofDays(2));
        Task task3 = new Task("task1", "des", Status.NEW, LocalDateTime.now(), Duration.ofDays(2));
        Task task2 = new Task("task2", "des", Status.NEW, LocalDateTime.now().plusDays(3), Duration.ofDays(2));
        Epic epic1 = new Epic("epic1", "des");

        imtm.createTask(task1);
        imtm.createTask(task2);
        imtm.createTask(task3);
        imtm.createEpic(epic1);

        Subtask subtask1 = new Subtask("subtask1", "des", Status.NEW, epic1.getId(), LocalDateTime.now().plusDays(10), Duration.ofDays(1));
        Subtask subtask2 = new Subtask("subtask2", "des", Status.IN_PROGRESS, epic1.getId(), LocalDateTime.now().plusDays(20), Duration.ofDays(2));

        imtm.createSubtask(subtask1);
        imtm.createSubtask(subtask2);

        System.out.println(imtm.getPrioritizedTasks());
    }

}
