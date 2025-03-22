package service;

import java.util.*;

import task.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();

    private int id = 0;

    HistoryManager historyManager = new InMemoryHistoryManager();

    // Вывод всех задач

    private int generateId() {
        return ++id;
    }

    @Override
    public Collection<Task> getAllTask() {
        return tasks.values();
    }

    @Override
    public Collection<Epic> getAllEpic() {
        return epics.values();
    }

    @Override
    public Collection<Subtask> getAllSubtask() {
        return subtasks.values();
    }

    // Удаление всех задач

    @Override
    public void removeTasks() {
        tasks.clear();
    }

    @Override
    public void removeEpics() {
        for (Epic epic : epics.values()) {
            for (int subtaskId : epic.getEpicSubtask())
                subtasks.remove(subtaskId);
        }
        epics.clear();
    }

    @Override
    public void removeSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getEpicSubtask().clear();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
    }

    // Получение задачи по id

    @Override
    public Task getByIdTask(int id) {
        if (tasks.containsKey(id)) {
            historyManager.addTaskInMapHistory(tasks.get(id));
        }
        return tasks.get(id);
    }

    @Override
    public Epic getByIdEpic(int id) {
        if (epics.containsKey(id)) {
            historyManager.addTaskInMapHistory(epics.get(id));

        }
        return epics.get(id);
    }

    @Override
    public Subtask getByIdSubtask(int id) {
        if (subtasks.containsKey(id)) {
            historyManager.addTaskInMapHistory(subtasks.get(id));
        }
        return subtasks.get(id);
    }

    // Создание задач

    @Override
    public int createTask(Task task) {
        if (task == null) {
            return -1;
        }

        int idTask = generateId();
        task.setId(idTask);
        tasks.put(idTask, task);
        return idTask;
    }

    @Override
    public int createEpic(Epic epic) {
        if (epic == null) {
            return -1;
        }

        int idEpic = generateId();
        epic.setId(idEpic);
        epics.put(idEpic, epic);
        return idEpic;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        if (subtask == null) {
            return -1;
        }

        int idSubtask = generateId();
        subtask.setId(idSubtask);
        Epic epic = epics.get(subtask.getEpicId());

        if (epic == null) {
            return -1;
        }

        subtasks.put(idSubtask, subtask);
        epic.addEpicSubtask(idSubtask);
        updateEpicStatus(epic.getId());
        return idSubtask;
    }

    // Изменение задачи

    @Override
    public int updateTask(Task task) {
        if (task == null && !tasks.containsKey(task.getId())) {
            return -1;
        }

        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public int updateEpic(Epic epic) {
        if (epic == null && !epics.containsKey(epic.getId())) {
            return -1;
        }

        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
        return epic.getId();
    }

    @Override
    public int updateSubtask(Subtask subtask) {
        if (subtask == null && !subtasks.containsKey(subtask.getId())) {
            return -1;
        }

        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getId());
        return subtask.getId();
    }

    // Удаление задач по id

    @Override
    public int removeTaskById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            return 1;
        }
        return -1;
    }

    @Override
    public int removeEpicById(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            if (epic == null) {
                return -1;
            }
            for (int subtaskId : epic.getEpicSubtask()) {
                subtasks.remove(subtaskId);
            }
            epics.remove(id);
            return 1;
        }
        return -1;
    }

    @Override
    public int removeSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskById(id);
            }
            subtasks.remove(id);
            return 1;
        }
        return -1;
    }

    // Дополнительные методы

    // Возврат Subtask по Epic
    @Override
    public List<Subtask> getEpicSubtask(Epic epic) {
        if (epic == null) {
            return new ArrayList<>();
        }

        List<Subtask> listSubtask = new ArrayList<>();

        for (Integer i : epic.getEpicSubtask()) {
            listSubtask.add(subtasks.get(i));
        }
        return listSubtask;
    }

    // Изменение статуса Epic и Subtask

    @Override
    public void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Integer> subtaskIds = epic.getEpicSubtask();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        // флаги для статуса задач
        boolean allNew = true;
        boolean allDone = true;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }

            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }

            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }
}
