package service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import task.*;

public class InMemoryTaskManagerTest {

    TaskManager tm = Managers.getDefaultTaskManager();

    // Создание задач

    @Test
    void getAllTaskTest() {
        Task task1 = new Task("task1", "des1", Status.NEW);
        Task task2 = new Task("task2", "des2", Status.NEW);
        Task task3 = new Task("task3", "des3", Status.IN_PROGRESS);
        tm.createTask(task1);
        tm.createTask(task2);
        tm.createTask(task3);
        assertEquals(3, tm.getAllTask().size());
    }

    @Test
    void removeEpicsTest() {
        Epic epic1 = new Epic("epic1", "des1");
        tm.createEpic(epic1);
        Subtask subtask1 = new Subtask("subtask1", "des1", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des2", epic1.getId());
        tm.createSubtask(subtask1);
        tm.createSubtask(subtask2);
        tm.removeEpics();
        assertEquals(0, tm.getAllEpic().size());
    }

    @Test
    void createTaskTest() {
        Task task1 = new Task("task1", "des1", Status.NEW);
        assertEquals(tm.createTask(task1), task1.getId());
    }

    @Test
    void createSubtaskTest() {
        Epic epic1 = new Epic("epic1", "des1");
        tm.createEpic(epic1);
        Subtask subtask1 = new Subtask("subtask1", "des1", epic1.getId());
        assertEquals(tm.createSubtask(subtask1), subtask1.getId());
    }

    @Test
    void createSubtaskCountTest() {
        Epic epic1 = new Epic("epic1", "des1");
        tm.createEpic(epic1);
        Subtask subtask1 = new Subtask("subtask1", "des1", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des2", epic1.getId());
        tm.createSubtask(subtask1);
        tm.createSubtask(subtask2);
        assertEquals(2, epic1.getEpicSubtask().size());
    }

    @Test
    void updateTaskTest() {
        Task task1 = new Task("task1", "des1", Status.NEW);
        tm.createTask(task1);
        task1.setStatus(Status.DONE);
        assertEquals(tm.updateTask(task1), task1.getId());
    }

    @Test
    void updateTaskNotIdTest(){
        Task task1 = new Task("task1", "des1", Status.NEW);
        Task task2 = new Task("task1", "des1", Status.NEW);
        tm.createTask(task1);
        tm.updateTask(task2);
        assertEquals(-1, tm.updateTask(task2));
    }

    @Test
    void getByIdTaskTest() {
        Task task1 = new Task("task1", "des", Status.NEW);
        Task task2 = new Task("task2", "des", Status.NEW);
        tm.createTask(task1);
        tm.createTask(task2);
        assertEquals(tm.getByIdTask(task2.getId()), task2);
    }

    @Test
    void removeTaskByIdTest() {
        Task task1 = new Task("task1", "des", Status.NEW);
        Task task2 = new Task("task2", "des", Status.NEW);
        tm.createTask(task1);
        tm.createTask(task2);
        assertEquals(1, tm.removeTaskById(task2.getId()));
    }

    @Test
    void removeTaskByIdNotFoundTest() {
        int nonId = -1;
        assertEquals(-1, tm.removeTaskById(nonId));
    }

    @Test
    void removeEpicByIdTest() {
        Epic epic1 = new Epic("epic1", "des1");
        Subtask subtask1 = new Subtask("subtask1", "des2", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des3", epic1.getId());
        tm.createEpic(epic1);
        tm.createSubtask(subtask1);
        tm.createSubtask(subtask2);
        assertEquals(1, tm.removeEpicById(epic1.getId()));
    }

    @Test
    void getEpicSubtaskTest() {
        Epic epic1 = new Epic("epic1", "des");

        tm.createEpic(epic1);

        Subtask subtask1 = new Subtask("subtask1", "des", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des", epic1.getId());

        tm.createSubtask(subtask1);
        tm.createSubtask(subtask2);

        assertEquals(subtask1, tm.getEpicSubtask(epic1).get(0));
        assertEquals(subtask2, tm.getEpicSubtask(epic1).get(1));
    }
}