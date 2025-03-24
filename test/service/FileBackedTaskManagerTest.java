package service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Status;
import task.Task;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FileBackedTaskManagerTest {

    Path directory = Paths.get("E:\\prj\\java-kanban\\src\\resources");
    String fileName = "test.csv";
    File file;
    Task task1;
    Task task2;
    FileBackedTaskManager fileBackedTaskManager;

    @BeforeEach
    public void beforeEach() {
        file = directory.resolve(fileName).toFile();
        fileBackedTaskManager = (FileBackedTaskManager) Managers.getFileBackedTaskManager(file);
        task1 = new Task("task1", "des", Status.NEW);
        task2 = new Task("task2", "des", Status.NEW);
    }

    @Test
    void saveTest() {
        fileBackedTaskManager.createTask(task1);
        assertTrue(file.length() > 0);
    }

    @Test
    void loadFile() {
        fileBackedTaskManager.createTask(task1);
        fileBackedTaskManager.createTask(task2);
        FileBackedTaskManager newTaskManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(2, newTaskManager.getAllTask().size());
    }
}
