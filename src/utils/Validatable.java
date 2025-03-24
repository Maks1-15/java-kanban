package utils;

import task.Task;

public interface Validatable {

    // Проверка при создании задачи
    default boolean isValidateToAddTask(Task task) {
        return task != null && !task.getName().isEmpty() && !task.getName().isBlank();
    }

    // Проверка для изменения или удаления
    default boolean isValidateUpdateTask(Task task) {
        return isValidateToAddTask(task) && task.getId() > 0;
    }

    // Проверка при добавлении в множество
    default boolean isValidateToAddTaskInSet(Task task) {
        return task.getStartTime() != null && task.getDuration() != null;
    }

}
