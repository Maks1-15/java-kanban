package service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class InMemoryTaskManagerTest {

    private InMemoryTaskManager tm;

    @BeforeEach
    void beforeEach() {
        tm = (InMemoryTaskManager) Managers.getDefaultTaskManager();
    }

    // Тесты для Task

    @Test
    void getAllTaskTest() {
        // Создаем несколько задач
        Task task1 = new Task("task1", "des1", Status.NEW);
        Task task2 = new Task("task2", "des2", Status.NEW);
        Task task3 = new Task("task3", "des3", Status.IN_PROGRESS);

        // Добавляем задачи в TaskManager
        tm.createTask(task1);
        tm.createTask(task2);
        tm.createTask(task3);

        // Проверяем, что количество задач в TaskManager соответствует ожидаемому
        assertEquals(3, tm.getAllTask().size());
    }

    @Test
    void getAllTaskEmptyTest() {
        // Проверяем, что TaskManager пуст, когда в нем нет задач
        assertEquals(0, tm.getAllTask().size());
    }

    @Test
    void createTaskTest() {
        // Создаем новую задачу
        Task task1 = new Task("task1", "des1", Status.NEW);

        // Создаем задачу в TaskManager
        int taskId = tm.createTask(task1);

        // Проверяем, что задача была успешно создана и ID был возвращен (ID > 0)
        assertTrue(taskId > 0);
    }

    @Test
    void updateTaskStatusTest() {
        // Создаем задачу
        Task task1 = new Task("task1", "des1", Status.NEW);
        int id = tm.createTask(task1);

        // Получаем задачу из TaskManager по ID
        Task taskToUpdate = tm.getByIdTask(id).orElse(null);

        assertNotNull(taskToUpdate);

        // Изменяем статус задачи
        taskToUpdate.setStatus(Status.DONE);

        // Обновляем задачу в TaskManager
        tm.updateTask(taskToUpdate);

        // Получаем обновленную задачу из TaskManager
        Task updatedTask = tm.getByIdTask(id).orElse(null);

        assertNotNull(updatedTask);

        // Проверяем, что статус задачи был успешно обновлен
        assertEquals(Status.DONE, updatedTask.getStatus());
    }

    @Test
    void getByIdTaskTest() {
        // Создаем задачу
        Task task1 = new Task("task1", "des", Status.NEW);
        int idTask1 = tm.createTask(task1);

        // Получаем задачу по ID
        Task retrievedTask = tm.getByIdTask(idTask1).orElse(null);

        // Проверяем, что задача была успешно получена
        assertNotNull(retrievedTask, "Задача не должна быть null");

        // Проверяем, что полученная задача соответствует ожидаемой
        assertEquals(task1.getName(), retrievedTask.getName());
        assertEquals(task1.getDescription(), retrievedTask.getDescription());
        assertEquals(task1.getStatus(), retrievedTask.getStatus());
    }

    @Test
    void removeTaskByIdTest() {
        // Создаем задачу
        Task task1 = new Task("task1", "des", Status.NEW);
        int idTask1 = tm.createTask(task1);

        // Удаляем задачу по ID
        int result = tm.removeTaskById(idTask1);

        // Проверяем, что задача была успешно удалена
        assertEquals(1, result);

        // Проверяем, что задача больше не существует в TaskManager
        assertNull(tm.getByIdTask(idTask1).orElse(null));
    }

    @Test
    void removeTaskByIdNotFoundTest() {
        // Пытаемся удалить задачу с несуществующим ID
        int nonExistentId = -1;
        int result = tm.removeTaskById(nonExistentId);

        // Проверяем, что удаление не удалось
        assertEquals(-1, result);
    }

    // Тесты для Epic и Subtask

    @Test
    void createEpicTest() {
        // Создаем Epic
        Epic epic1 = new Epic("epic1", "des1");

        // Создаем Epic в TaskManager
        int epicId = tm.createEpic(epic1);

        // Проверяем, что Epic был успешно создан и ID был возвращен (ID > 0)
        assertTrue(epicId > 0);
    }

    @Test
    void createSubtaskTest() {
        // Создаем Epic
        Epic epic1 = new Epic("epic1", "des1");
        int epicId = tm.createEpic(epic1);

        // Создаем Subtask, связанную с Epic
        Subtask subtask1 = new Subtask("subtask1", "des1", epicId);

        // Создаем Subtask в TaskManager
        int subtaskId = tm.createSubtask(subtask1);

        // Проверяем, что Subtask был успешно создан и ID был возвращен (ID > 0)
        assertTrue(subtaskId > 0, "ID новой Subtask должен быть больше 0");

        // Проверяем, что Subtask была добавлена в список Subtask Epic
        assertEquals(1, epic1.getEpicSubtask().size());
    }

    @Test
    void createSubtaskCountTest() {
        // Создаем Epic
        Epic epic1 = new Epic("epic1", "des1");
        tm.createEpic(epic1);

        // Создаем несколько Subtask, связанных с Epic
        Subtask subtask1 = new Subtask("subtask1", "des1", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des2", epic1.getId());
        tm.createSubtask(subtask1);
        tm.createSubtask(subtask2);

        // Проверяем, что количество Subtask Epic соответствует ожидаемому
        assertEquals(2, epic1.getEpicSubtask().size());
    }

    @Test
    void removeEpicsTest() {
        // Создаем Epic
        Epic epic1 = new Epic("epic1", "des1");
        tm.createEpic(epic1);

        // Создаем несколько Subtask, связанных с Epic
        Subtask subtask1 = new Subtask("subtask1", "des1", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des2", epic1.getId());
        tm.createSubtask(subtask1);
        tm.createSubtask(subtask2);

        // Удаляем все Epic
        tm.removeEpics();

        // Проверяем, что количество Epic в TaskManager равно 0
        assertEquals(0, tm.getAllEpic().size(), "Количество Epic должно быть 0");

        // Проверяем, что количество Subtask в TaskManager равно 0
        assertEquals(0, tm.getAllSubtask().size());
    }

    @Test
    void removeEpicByIdTest() {
        // Создаем Epic
        Epic epic1 = new Epic("epic1", "des1");
        int epicId = tm.createEpic(epic1);

        // Создаем несколько Subtask, связанных с Epic
        Subtask subtask1 = new Subtask("subtask1", "des2", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des3", epic1.getId());
        tm.createSubtask(subtask1);
        tm.createSubtask(subtask2);

        // Удаляем Epic по ID
        assertEquals(1, tm.removeEpicById(epicId));

        // Проверяем, что Epic больше не существует в TaskManager
        assertNull(tm.getByIdEpic(epicId).orElse(null));

        // Проверяем, что Subtask больше не существуют в TaskManager
        assertEquals(0, tm.getAllSubtask().size());
    }

    @Test
    void getEpicSubtaskTest() {
        // Создаем Epic
        Epic epic1 = new Epic("epic1", "des");
        tm.createEpic(epic1);

        // Создаем несколько Subtask, связанных с Epic
        Subtask subtask1 = new Subtask("subtask1", "des", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des", epic1.getId());

        int idSubtask1 = tm.createSubtask(subtask1);
        int idSubtask2 = tm.createSubtask(subtask2);

        // Получаем Subtask Epic из TaskManager
        List<Subtask> epicSubtasks = tm.getEpicSubtask(epic1);

        // Проверяем, что количество Subtask Epic соответствует ожидаемому
        assertEquals(2, epicSubtasks.size());

        // Проверяем, что Subtask Epic соответствуют ожидаемым
        assertEquals(tm.subtasks.get(idSubtask1), epicSubtasks.get(0));
        assertEquals(tm.subtasks.get(idSubtask2), epicSubtasks.get(1));
    }

    @Test
    void StatusNewEpicTest() {
        // Создаем Epic
        Epic epic1 = new Epic("epic1", "des");
        tm.createEpic(epic1);

        // Создаем несколько Subtask, связанных с Epic
        Subtask subtask1 = new Subtask("subtask1", "des", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des", epic1.getId());
        tm.createSubtask(subtask1);
        tm.createSubtask(subtask2);

        // Проверяем, что статус Epic соответствует ожидаемому (NEW)
        assertEquals(Status.NEW, epic1.getStatus(), "Статус Epic должен быть NEW");
    }

    @Test
    void StatusDoneEpicTest() {
        // Создаем Epic
        Epic epic1 = new Epic("epic1", "des");
        tm.createEpic(epic1);

        // Создаем несколько Subtask, связанных с Epic
        Subtask subtask1 = new Subtask("subtask1", "des", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des", epic1.getId());
        int idSubtask1 = tm.createSubtask(subtask1);
        int idSubtask2 = tm.createSubtask(subtask2);

        // Получаем Subtask из TaskManager и изменяем их статус на DONE
        Subtask doneSubtask1 = tm.subtasks.get(idSubtask1);
        Subtask doneSubtask2 = tm.subtasks.get(idSubtask2);

        doneSubtask1.setStatus(Status.DONE);
        doneSubtask2.setStatus(Status.DONE);

        tm.updateSubtask(doneSubtask1);
        tm.updateSubtask(doneSubtask2);

        tm.updateEpicStatus(epic1.getId());

        // Проверяем, что статус Epic соответствует ожидаемому (DONE)
        assertEquals(Status.DONE, epic1.getStatus());
    }

    @Test
    void StatusDoneAndNewEpicTest() {
        // Создаем Epic
        Epic epic1 = new Epic("epic1", "des");
        tm.createEpic(epic1);

        // Создаем несколько Subtask, связанных с Epic
        Subtask subtask1 = new Subtask("subtask1", "des", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des", epic1.getId());
        int idSubtask1 = tm.createSubtask(subtask1);
        int idSubtask2 = tm.createSubtask(subtask2);

        // Получаем Subtask из TaskManager и изменяем статус одного на DONE
        Subtask doneSubtask1 = tm.subtasks.get(idSubtask1);
        doneSubtask1.setStatus(Status.DONE);
        tm.updateSubtask(doneSubtask1);

        tm.updateEpicStatus(epic1.getId());

        // Проверяем, что статус Epic соответствует ожидаемому (IN_PROGRESS)
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    void StatusInProgressEpicTest() {
        // Создаем Epic
        Epic epic1 = new Epic("epic1", "des");
        tm.createEpic(epic1);

        // Создаем несколько Subtask, связанных с Epic
        Subtask subtask1 = new Subtask("subtask1", "des", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "des", epic1.getId());
        int idSubtask1 = tm.createSubtask(subtask1);
        int idSubtask2 = tm.createSubtask(subtask2);

        // Получаем Subtask из TaskManager и изменяем их статус на IN_PROGRESS
        Subtask inProgressSubtask1 = tm.subtasks.get(idSubtask1);
        Subtask inProgressSubtask2 = tm.subtasks.get(idSubtask2);

        inProgressSubtask1.setStatus(Status.IN_PROGRESS);
        inProgressSubtask2.setStatus(Status.IN_PROGRESS);

        tm.updateSubtask(inProgressSubtask1);
        tm.updateSubtask(inProgressSubtask2);

        tm.updateEpicStatus(epic1.getId());

        // Проверяем, что статус Epic соответствует ожидаемому (IN_PROGRESS)
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    // Тесты для PrioritizedTasksSet

    @Test
    void sizePrioritizedTasksSetTest() {
        // Создаем несколько задач с указанием времени и длительности
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("task1", "des", Status.NEW, now, Duration.ofDays(1));
        Task task2 = new Task("task2", "des", Status.NEW, now.plusDays(3), Duration.ofDays(1));
        tm.createTask(task1);
        tm.createTask(task2);

        // Проверяем, что количество задач в PrioritizedTasksSet соответствует ожидаемому
        assertEquals(2, tm.getPrioritizedTasks().size());
    }

    @Test
    void sizeOnePrioritizedTasksSetTest() {
        // Создаем несколько задач с указанием времени и длительности
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("task1", "des", Status.NEW, now, Duration.ofDays(1));
        Task task2 = new Task("task2", "des", Status.NEW);
        tm.createTask(task1);
        tm.createTask(task2);

        // Проверяем, что количество задач в PrioritizedTasksSet соответствует ожидаемому
        assertEquals(1, tm.getPrioritizedTasks().size());
        assertEquals(2, tm.tasks.size());
    }

    @Test
    void sizeDeleteFirstTaskPrioritizedTasksSetTest() {
        // Создаем несколько задач с указанием времени и длительности
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("task1", "des", Status.NEW, now, Duration.ofDays(1));
        Task task2 = new Task("task2", "des", Status.NEW, now.plusDays(3), Duration.ofDays(1));

        // Добавляем задачи в TaskManager
        int id1 = tm.createTask(task1);
        tm.createTask(task2);

        // Удаляем первую задачу
        tm.removeTaskById(id1);

        // Проверяем, что количество задач в PrioritizedTasksSet после удаления соответствует ожидаемому
        assertEquals(1, tm.getPrioritizedTasks().size());
    }

    @Test
    void intervalIntersectionTest() {
        // Создаем две задачи, которые пересекаются по времени
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("task1", "des", Status.NEW, now, Duration.ofDays(1));
        Task task2 = new Task("task2", "des", Status.NEW, now, Duration.ofDays(1));

        // Добавляем задачи в TaskManager
        tm.createTask(task1);
        tm.createTask(task2);

        // Проверяем, что в PrioritizedTasksSet только одна задача (из-за пересечения)
        assertEquals(1, tm.getPrioritizedTasks().size());
    }

    @Test
    void intervalNotIntersectionTest() {
        // Создаем две задачи, которые не пересекаются по времени
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("task1", "des", Status.NEW, now, Duration.ofDays(1));
        Task task2 = new Task("task2", "des", Status.NEW, now.plusDays(2), Duration.ofDays(1));

        // Добавляем задачи в TaskManager
        tm.createTask(task1);
        tm.createTask(task2);

        // Проверяем, что в PrioritizedTasksSet две задачи (нет пересечения)
        assertEquals(2, tm.getPrioritizedTasks().size());
    }

    @Test
    void updateTaskNotIdTest() {
        // Создаем две задачи с одинаковыми данными, но разными ID
        Task task1 = new Task("task1", "des1", Status.NEW);
        Task task2 = new Task("task2", "des2", Status.NEW);

        // Добавляем первую задачу в TaskManager
        int idTask1 = tm.createTask(task1);

        // Пытаемся обновить TaskManager второй задачей (с другим ID)
        int result = tm.updateTask(task2);

        // Проверяем, что обновление не удалось
        assertEquals(-1, result);
    }

    @Test
    void updateTaskCloneStatusTest() {
        // Создаем задачу
        Task task1 = new Task("task1", "des1", Status.NEW);

        // Добавляем задачу в TaskManager и получаем ее ID
        int id = tm.createTask(task1);

        // Получаем задачу из TaskManager по ID
        Task taskFromManager = tm.getByIdTask(id).orElse(null);

        assertNotNull(taskFromManager);

        // Создаем еще одну задачу на основе первой
        Task task2 = new Task("task1", "des1", Status.NEW);
        task2.setId(id);

        // Изменяем статус второй задачи
        task2.setStatus(Status.DONE);

        // Обновляем задачу в TaskManager
        tm.updateTask(task2);

        // Снова получаем задачу из TaskManager по ID
        Task updatedTaskFromManager = tm.getByIdTask(id).orElse(null);

        assertNotNull(updatedTaskFromManager);

        // Проверяем, что статус первой задачи не изменился (клонирование)
        assertNotEquals(task1.getStatus(), updatedTaskFromManager.getStatus());
    }

    @Test
    void notUpdateTaskCloneStatusTest() {
        // Создаем задачу
        Task task1 = new Task("task1", "des1", Status.NEW);
        Task task2 = new Task("task2", "des2", Status.NEW);

        int idTask1 = tm.createTask(task1);
        int idTask2 = tm.createTask(task2);

        // меняем статус, но без update
        task1.setStatus(Status.DONE);

        assertEquals(Status.NEW, tm.tasks.get(idTask1).getStatus());
    }
}
