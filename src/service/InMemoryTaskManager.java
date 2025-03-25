package service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import task.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();

    private final TreeSet<Task> prioritizedTasksSet = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    HistoryManager historyManager = Managers.getDefaultHistoryManager();

    private int id = 0;

    private int generateId() {
        return ++id;
    }

    // Валидация данных, проверка на время и null

    private boolean isValidateDateAndDuration(Task task) {
        return task.getStartTime() != null && task.getDuration() != null;
    }

    private boolean isValidateName(Task task) {
        return task != null && !task.getName().isEmpty() && !task.getName().isBlank();
    }

    private boolean isAllTasksNotOverlap(Task task) {
        // лучше обращаться напрямую к Set или взять его копию List?
        if (!(isValidateName(task) && isValidateDateAndDuration(task))) {
            return false;
            // проверка задачи и наличие времени
        }

        if (prioritizedTasksSet.isEmpty()) {
            return true;
        }

        LocalDateTime taskStart = task.getStartTime();
        LocalDateTime taskEnd = task.getEndTime();

        // Проверяем, что новая задача не пересекается ни с одной из существующих
        return prioritizedTasksSet.stream().allMatch(task1 -> {
            LocalDateTime existingStart = task1.getStartTime();
            LocalDateTime existingEnd = task1.getEndTime();

            // Два интервала не пересекаются, если один заканчивается раньше, чем начинается другой
            return taskEnd.isBefore(existingStart) || taskStart.isAfter(existingEnd);
        });


    }

    // Вывод списка всех приоритетных задач
    public Collection<Task> getPrioritizedTasks() {
        return prioritizedTasksSet.stream().toList();
    }

    // Вывод всех задач

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

    // Удаление всех задач, переписанные с помощью stream Api

    @Override
    public void removeTasks() {
        tasks.clear();
    }

    @Override
    public void removeEpics() {
        epics.values().stream().
                flatMap(epic -> epic.getEpicSubtask()
                        .stream())
                .forEach(subtasks::remove);
        epics.clear();
    }

    // В этом методе как-то не догадался как именно через Stream. И не подскажешь, тут же может возникнуть исключение
    // т.к. мы меняем коллекцию. Чтобы избежать этого, я должен создать новую коллекцию и с ней проводить манипуляции или как?
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

    // Еще вопрос, мы должны работать с объектами напрямую, или брать их копии?
    // ситуация в том, что любую задачу мы можем изменить путем использования settera, не прибегая к методу update,
    // как я понимаю, это не доработка и это нужно исправить путем создания копии задачи и работы с ней, так?

    @Override
    public int createTask(Task task) {
        if (!isValidateName(task)) {
            return -1;
        }

        int idTask = generateId();
        task.setId(idTask);
        tasks.put(idTask, task);

        if (isAllTasksNotOverlap(task)) {
            prioritizedTasksSet.add(task);
        }

        return idTask;
    }

    @Override
    public int createEpic(Epic epic) {
        if (!isValidateName(epic)) {
            return -1;
        }

        int idEpic = generateId();
        epic.setId(idEpic);
        epics.put(idEpic, epic);
        return idEpic;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        if (!isValidateName(subtask)) {
            return -1;
        }

        int idSubtask = generateId();
        subtask.setId(idSubtask);
        Epic epic = epics.get(subtask.getEpicId());

        if (!isValidateName(epic)) {
            return -1;
        }

        subtasks.put(idSubtask, subtask);
        epic.addEpicSubtask(idSubtask);
        updateEpicStatus(epic.getId());

        if (isAllTasksNotOverlap(subtask)) {
            prioritizedTasksSet.add(subtask);
        }

        return idSubtask;
    }

    // Изменение задачи

    @Override
    public int updateTask(Task task) {
        if (!isValidateName(task) || !tasks.containsKey(task.getId())) {
            return -1;
        }

        tasks.put(task.getId(), task);

        if (isAllTasksNotOverlap(task)) {
            prioritizedTasksSet.add(task);
        }
        return task.getId();
    }

    @Override
    public int updateEpic(Epic epic) {
        if (!isValidateName(epic) || !epics.containsKey(epic.getId())) {
            return -1;
        }

        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
        return epic.getId();
    }

    @Override
    public int updateSubtask(Subtask subtask) {
        if (!isValidateName(subtask) || !subtasks.containsKey(subtask.getId())) {
            return -1;
        }

        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getId());

        if (isAllTasksNotOverlap(subtask)) {
            prioritizedTasksSet.add(subtask);
        }

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

    // Возврат Subtask по Epic, измененный Api
    @Override
    public List<Subtask> getEpicSubtask(Epic epic) {
        if (epic == null) {
            return new ArrayList<>();
        }

        return epic.getEpicSubtask().stream()
                .map(subtasks::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistoryList());
    }

    // Методы для работы с Epic

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

    // Возвращает последнюю подзадачу из Epic
    @Override
    public Subtask getSubtaskMaxEndTime() {
        // дописать реализацию через optional
        if (subtasks == null || subtasks.isEmpty()) {
            throw new IllegalStateException("Нет подзадач");
        }
        Optional<Subtask> subtaskMax = subtasks.values().stream()
                .max(Comparator.comparing(subtask -> subtask.getStartTime().plus(subtask.getDuration())));
        if (subtaskMax.isPresent()) {
            return subtaskMax.get();
        }
        throw new IllegalStateException("Подзадача не найдена");
    }

    // Возвращает первую подзадачу из Epic
    @Override
    public Subtask getSubtaskMinStartTime() {
        if (subtasks == null || subtasks.isEmpty()) {
            throw new IllegalStateException("Нет подзадач");
        }
        Optional<Subtask> subtaskMin = subtasks.values().stream()
                .min(Comparator.comparing(Subtask::getStartTime));
        if (subtaskMin.isPresent()) {
            return subtaskMin.get();
        }
        throw new IllegalStateException("Подзадача не найдена");
    }

    // Возвращает время начала Epic
    @Override
    public LocalDateTime calculateEpicStartTime() {
        Subtask subtaskStartTime = getSubtaskMinStartTime();
        return subtaskStartTime.getStartTime();
    }

    // Возвращает время начала Epic
    @Override
    public LocalDateTime calculateEpicEndTime() {
        Subtask subtaskMaxEndTime = getSubtaskMaxEndTime();
        return subtaskMaxEndTime.getStartTime().plus(subtaskMaxEndTime.getDuration());
    }

    // Возвращает продолжительность Epic
    // Проходим по коллекции, в которой точно есть дата начала, отбираем подзадачи и
    // и суммируем все в одну
    @Override
    public Duration getDuration() {
        return prioritizedTasksSet.stream()
                .filter(Subtask.class::isInstance)
                .map(Task::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }
}
