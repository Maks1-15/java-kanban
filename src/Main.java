import task.*;
import service.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {

        Path directory = Paths.get("E:\\prj\\java-kanban\\src\\resources");
        String fileName = "test.csv";
        File file = directory.resolve(fileName).toFile();

        FileBackedTaskManager FileBackedTaskManager = (FileBackedTaskManager) Managers.getFileBackedTaskManager(file);

        Task task1 = new Task("task1", "des", Status.NEW);
        Task task2 = new Task("task2", "des", Status.NEW);
        Epic epic1 = new Epic("epic1", "des");
        Epic epic2 = new Epic("epic2", "des");

        FileBackedTaskManager.createTask(task1);
        FileBackedTaskManager.createTask(task2);
        FileBackedTaskManager.createEpic(epic1);
        FileBackedTaskManager.createEpic(epic2);

        Subtask subtask1 = new Subtask("subtask1", "des", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des", epic1.getId());
        Subtask subtask3 = new Subtask("subtask3", "des", epic1.getId());

        FileBackedTaskManager.createSubtask(subtask1);
        FileBackedTaskManager.createSubtask(subtask2);
        FileBackedTaskManager.createSubtask(subtask3);

        FileBackedTaskManager.save();

        FileBackedTaskManager newTaskManager = FileBackedTaskManager.loadFromFile(file);

        if (newTaskManager != null) {
            System.out.println("Задачи после загрузки:");
            System.out.println(newTaskManager.getAllTask());
            System.out.println(newTaskManager.getAllEpic());
            System.out.println(newTaskManager.getAllSubtask());
        } else {
            System.out.println("Задачи не загружены");
        }

    }
}
