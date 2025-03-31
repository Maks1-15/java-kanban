package service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import task.*;

public class InMemoryTaskManager implements TaskManager {

    // Хранилища для задач разных типов.
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();

    // Дерево для хранения приоритетных задач, отсортированных по времени начала.
    // Используем TreeSet для автоматической сортировки и исключения дубликатов, если
    // задачи начинаются в одно и то же время.  Если задачи имеют одинаковое время начала,
    // то нужно предусмотреть логику сравнения по другим полям, чтобы избежать потери данных в TreeSet.
    private final TreeSet<Task> prioritizedTasksSet = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    HistoryManager historyManager = Managers.getDefaultHistoryManager();

    private int idTask = 0;

    private int generateId() {
        return ++idTask;
    }

    //-------------------------------------------------------------------------
    // Валидация данных
    //-------------------------------------------------------------------------

    // Проверка, что у задачи указаны время начала и продолжительность
    private boolean isValidateDateAndDuration(Task task) {
        return task.getStartTime() != null && task.getDuration() != null;
    }

    // Проверка, что у задачи указано имя (не пустое и не состоит только из пробелов)
    private boolean isValidateName(Task task) {
        return task != null && !task.getName().isEmpty() && !task.getName().isBlank();
    }

    // Проверка на пересечение задач по времени
    private boolean isAllTasksNotOverlap(Task task) {
        // проверка на пустоту. Если задач нет, то пересечения не будет и можно добавлять
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

    //-------------------------------------------------------------------------
    // Вывод задач
    //-------------------------------------------------------------------------

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

    //-------------------------------------------------------------------------
    // Удаление всех задач
    //-------------------------------------------------------------------------

    // Удаление Tasks
    @Override
    public void removeTasks() {
        tasks.clear();
        prioritizedTasksSet.removeIf(task -> task instanceof Task); // проверяем тип задачи
    }

    // Удаление Epics
    @Override
    public void removeEpics() {
        epics.values().forEach(epic -> {
            epic.getEpicSubtask().forEach(subtasks::remove);
            prioritizedTasksSet.removeIf(task -> subtasks.containsKey(task.getId()));
        });

        // Очищаем список эпиков.
        epics.clear();
    }

    // Удаление Subtasks
    @Override
    public void removeSubtasks() {
        // Удаляем все подзадачи, связанные с эпиками.
        epics.values().forEach(epic -> {
            subtasks.remove(epic.getId());
            updateEpicStatus(epic.getId());
        });


        // Очищаем список подзадач.
        subtasks.clear();
        prioritizedTasksSet.removeIf(task -> task instanceof Subtask);
    }

    //-------------------------------------------------------------------------
    // Получение задачи по ID
    //-------------------------------------------------------------------------

    // Получение задачи по идентификатору
    @Override
    public Optional<Task> getByIdTask(int id) {
        if (tasks.containsKey(id)) {
            Task task = tasks.get(id);
            historyManager.addTaskInMapHistory(task);
            return Optional.ofNullable(task);
        }
        return Optional.empty(); // Возвращаем null, если задача не найдена
    }

    // Получение эпика по идентификатору
    @Override
    public Optional<Epic> getByIdEpic(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            historyManager.addTaskInMapHistory(epic);
            return Optional.ofNullable(epic);
        }
        return Optional.empty(); // Возвращаем null, если эпик не найден
    }

    // Получение подзадачи по идентификатору
    @Override
    public Optional<Subtask> getByIdSubtask(int id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            historyManager.addTaskInMapHistory(subtask);
            return Optional.ofNullable(subtask);
        }
        return Optional.empty(); // Возвращаем null, если подзадача не найдена
    }

    //-------------------------------------------------------------------------
    // Создание задач
    //-------------------------------------------------------------------------

    // Создание Task
    @Override
    public int createTask(Task task) {
        if (!isValidateName(task)) {
            return -1;
        }
        try {
            // Работаем с копией объекта, чтобы предотвратить неявное изменение задачи через сеттеры

            Task newTask = task.clone();
            int idTask = generateId();
            newTask.setId(idTask);

            if (isValidateDateAndDuration(task)) { // проверка, что у задачи есть время
                if (!isAllTasksNotOverlap(task)) { // проверка, что задача пересекается
                    return -1;
                }
                tasks.put(idTask, newTask); // если не пересекаются, то добавляем в мапу и в множество
                prioritizedTasksSet.add(newTask);
            } else {
                tasks.put(idTask, newTask); // если времени нет, то просто добавляем задачу в мапу
            }
            return idTask;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Создание Epic
    @Override
    public int createEpic(Epic epic) {
        if (!isValidateName(epic)) {
            return -1;
        }

        int idEpic = generateId();
        epic.setId(idEpic);
        epics.put(idEpic, epic); // добавляем в мапу
        return idEpic;
    }

    // Создание Subtask
    @Override
    public int createSubtask(Subtask subtask) {
        // комментарии аналогичны Task
        if (!isValidateName(subtask)) {
            return -1;
        }

        try {
            Subtask newSubtask = subtask.clone();
            int idSubtask = generateId();
            newSubtask.setId(idSubtask);

            Epic epic = epics.get(newSubtask.getEpicId()); // получаем соответствующий Epic

            if (epic == null) { // проверка на null
                return -1;
            }

            if (isValidateDateAndDuration(subtask)) {
                if (!isAllTasksNotOverlap(subtask)) {
                    return -1;
                }
                subtasks.put(idSubtask, newSubtask);
                prioritizedTasksSet.add(newSubtask);
            } else {
                subtasks.put(idSubtask, newSubtask);
            }
            // методы по изменению статуса Epic в зависимости от subtask
            epic.addEpicSubtask(idSubtask); // добавляет в хранилище лист EpicSubtaskId id subtask
            updateEpicStatus(epic.getId()); // меняет статус Epic
            return idSubtask;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    //-------------------------------------------------------------------------
    // Изменение задач
    //-------------------------------------------------------------------------

    @Override
    // Изменение Task
    public int updateTask(Task task) {
        if (!isValidateName(task) || !tasks.containsKey(task.getId())) {
            return -1;
        }

        if (!isValidateDateAndDuration(task)) {
            tasks.put(task.getId(), task);
            return task.getId();
        }
        // Перед обновлением задачи, удаляем её из prioritizedTasksSet, если она там есть
        prioritizedTasksSet.removeIf(t -> t.getId() == task.getId());

        if (!isAllTasksNotOverlap(task)) {
            return -1;
        }

        tasks.put(task.getId(), task);
        prioritizedTasksSet.add(task);
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

        if (!isValidateDateAndDuration(subtask)) {
            subtasks.put(subtask.getId(), subtask);
            return subtask.getId();
        }
        // обновляем статус
        updateEpicStatus(subtask.getId());

        // Перед обновлением подзадачи, удаляем её из prioritizedTasksSet, если она там есть
        prioritizedTasksSet.removeIf(s -> s.getId() == subtask.getId());

        if (!isAllTasksNotOverlap(subtask)) {
            return -1;
        }

        subtasks.put(subtask.getId(), subtask);
        prioritizedTasksSet.add(subtask);
        return subtask.getId();
    }

    //-------------------------------------------------------------------------
    // Удаление задач по id
    //-------------------------------------------------------------------------

    @Override
    public int removeTaskById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            prioritizedTasksSet.removeIf(task -> task.getId() == id);
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
            for (int subtaskId : epic.getEpicSubtask()) { // удаление подзадач Epic
                prioritizedTasksSet.removeIf(task -> task.getId() == subtaskId);
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
                updateEpicStatus(epic.getId()); // Обновляем статус эпика после удаления подзадачи
            }
            subtasks.remove(id);
            prioritizedTasksSet.removeIf(task -> task.getId() == id);
            return 1;
        }
        return -1;
    }

    //-------------------------------------------------------------------------
    // Дополнительные методы
    //-------------------------------------------------------------------------

    // Возврат Subtask по Epic, измененный Api
    @Override
    public List<Subtask> getEpicSubtask(Epic epic) {
        if (epic == null) {
            return new ArrayList<>();
        }

        // Преобразуем через stream поток данных, вызываем get и выводим
        return epic.getEpicSubtask().stream().map(subtasks::get).collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistoryList());
    }

    //-------------------------------------------------------------------------
    // Методы для работы с Epic
    //-------------------------------------------------------------------------

    // Изменение статуса Epic и Subtask

    @Override
    public void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Integer> subtaskIds = epic.getEpicSubtask();
        if (subtaskIds.isEmpty()) { // если пустой, то возвращаю статус new
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
    public Optional<Subtask> getSubtaskMaxEndTime() {
        return subtasks.values().stream()
                .max(Comparator.comparing(subtask -> subtask.getStartTime().plus(subtask.getDuration())));
        // если задачи нет, max вернет пустоту
    }

    // Возвращает первую подзадачу из Epic
    @Override
    public Optional<Subtask> getSubtaskMinStartTime() {
        return subtasks.values().stream()
                .min(Comparator.comparing(Subtask::getStartTime));
        // если задачи нет, max вернет пустоту
    }

    // Возвращает время начала Epic
    @Override
    public LocalDateTime calculateEpicStartTime() {
        Subtask subtaskStartTime = getSubtaskMinStartTime().orElseThrow();
        return subtaskStartTime.getStartTime();
    }

    // Возвращает время начала Epic
    @Override
    public LocalDateTime calculateEpicEndTime() {
        Subtask subtaskMaxEndTime = getSubtaskMaxEndTime().orElseThrow();
        return subtaskMaxEndTime.getStartTime().plus(subtaskMaxEndTime.getDuration());
    }

    // Возвращает продолжительность Epic
    // Проходим по коллекции, в которой точно есть дата начала, отбираем подзадачи и
    // и суммируем все в одну
    @Override
    public Duration getDuration() {
        return prioritizedTasksSet.stream().filter(Subtask.class::isInstance).map(Task::getDuration).reduce(Duration.ZERO, Duration::plus);
    }
}
