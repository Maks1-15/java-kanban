package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;


import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    TaskManager tm;
    HistoryManager hm;

    @BeforeEach
    public void beforeEach() {
        tm = new InMemoryTaskManager();
        hm = new InMemoryHistoryManager();
    }

    @Test
    void addTest() {
        Task task1 = new Task("task1", "des", Status.NEW);
        tm.createTask(task1);
        assertEquals(1, hm.addTaskInMapHistory(task1));
    }

//    @Test
//    void removeIdByHistoryMap() {
//        Task task1 = new Task("task1", "des", Status.NEW);
//        Task task2 = new Task("task2", "des", Status.NEW);
//        tm.createTask(task1);
//        tm.createTask(task2);
//        tm.getByIdTask(task1.getId());
//        tm.getByIdTask(task2.getId());
//        assertEquals(1, hm.removeIdByHistoryMap(task1.getId()));
//    }

    @Test
    void getHistoryTest() {

        Task task1 = new Task("task1", "des", Status.NEW);
        Task task2 = new Task("task2", "des", Status.NEW);
        Epic epic1 = new Epic("epic1", "des");

        tm.createTask(task1);
        tm.createTask(task2);
        tm.createEpic(epic1);

        Subtask subtask1 = new Subtask("subtask1", "des", epic1.getId());

        tm.createSubtask(subtask1);

        tm.getByIdTask(task1.getId());
        tm.getByIdTask(task2.getId());
        tm.getByIdEpic(epic1.getId());
        tm.getByIdSubtask(subtask1.getId());
        tm.getByIdEpic(epic1.getId());

        assertEquals(task1, tm.getHistory().get(0));
        assertEquals(task2, tm.getHistory().get(1));
        assertEquals(subtask1, tm.getHistory().get(2));
        assertEquals(epic1, tm.getHistory().get(3));
    }

}



