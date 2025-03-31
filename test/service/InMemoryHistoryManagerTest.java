package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    InMemoryTaskManager tm;
    InMemoryHistoryManager hm;

    @BeforeEach
    public void beforeEach() {
        tm = (InMemoryTaskManager) Managers.getDefaultTaskManager();
        hm = (InMemoryHistoryManager) Managers.getDefaultHistoryManager();
    }

    @Test
    void addTest() {
        Task task1 = new Task("task1", "des", Status.NEW);
        int idTask1 = tm.createTask(task1);
        assertEquals(1, hm.addTaskInMapHistory(tm.tasks.get(idTask1)));
    }

    @Test
    void getHistoryTest() {

        Task task1 = new Task("task1", "des", Status.NEW, LocalDateTime.now(), Duration.ofDays(1));
        Task task2 = new Task("task2", "des", Status.NEW, LocalDateTime.now().plusDays(3), Duration.ofDays(1));
        Epic epic1 = new Epic("epic1", "des");

        int idTask1 = tm.createTask(task1);
        int idTask2 = tm.createTask(task2);
        int idEpic1 = tm.createEpic(epic1);

        Subtask subtask1 = new Subtask("subtask1", "des", Status.NEW, epic1.getId(), LocalDateTime.now().plusWeeks(1), Duration.ofHours(1));

        int idSubtask1 = tm.createSubtask(subtask1);

        tm.getByIdTask(tm.tasks.get(idTask1).getId());
        tm.getByIdTask(tm.tasks.get(idTask2).getId());
        tm.getByIdEpic(tm.epics.get(idEpic1).getId());
        tm.getByIdSubtask(tm.subtasks.get(idSubtask1).getId());
        tm.getByIdEpic(tm.epics.get(idEpic1).getId());
        System.out.println(tm.getHistory());

        assertEquals(tm.tasks.get(idTask1), tm.getHistory().get(0));
        assertEquals(tm.tasks.get(idTask2), tm.getHistory().get(1));
        assertEquals(tm.subtasks.get(idSubtask1), tm.getHistory().get(2));
        assertEquals(tm.epics.get(idEpic1), tm.getHistory().get(3));
    }

}



