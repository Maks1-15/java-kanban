package service;

import task.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    // получаем файл в конструкторе (надо написать метод для создания файла, file == null)
    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");
            writeTasks(writer, getAllTask());
            writeTasks(writer, getAllEpic());
            writeTasks(writer, getAllSubtask());

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла");
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager FileBackedTaskManager = new FileBackedTaskManager(file);
        if (!file.exists()) {
            throw new ManagerSaveException("Ошибка, файл пуст");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // не читаем 1 строку, т.к там нет нужной инфы
            reader.readLine();
            List<Integer> listId = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                writeIdToFile(line);
                try {
                    Task task = parseTaskFromString(line);
                    if (task instanceof Epic) {
                        FileBackedTaskManager.epics.put(task.getId(), (Epic) task);
                    } else if (task instanceof Subtask) {
                        FileBackedTaskManager.subtasks.put(task.getId(), (Subtask) task);
                    } else {
                        FileBackedTaskManager.tasks.put(task.getId(), task);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FileBackedTaskManager;
    }

    private <T extends Task> void writeTasks(BufferedWriter writer, Collection<T> tasks) throws IOException {
        for (T task : tasks) {
            writer.write(task.toFileString() + System.lineSeparator());
        }
    }

    private static void writeIdToFile(String line) {
        List<Integer> listId = new ArrayList<>();
        String[] arrayString = line.split(",");

        int id = Integer.parseInt(arrayString[0]);
        listId.add(id);
    }


    private static Task parseTaskFromString(String line) {
        String[] arrayString = line.split(",");

        int id = Integer.parseInt(arrayString[0]);
        TaskStatus taskStatus = TaskStatus.valueOf(arrayString[1]);
        String name = arrayString[2];
        String description = arrayString[3];
        Status status = Status.valueOf(arrayString[4]);

        switch (taskStatus) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK: {
                int epicId = Integer.parseInt(arrayString[5]);
                return new Subtask(id, name, description, status, epicId);
            }
            default:
                System.out.println("Неизвестная задача");
        }
        return null;
    }

    @Override
    public int createEpic(Epic epic) {
        save();
        return super.createEpic(epic);
    }

    @Override
    public int createTask(Task task) {
        save();
        return super.createTask(task);
    }

    @Override
    public int createSubtask(Subtask subtask) {
        save();
        return super.createSubtask(subtask);
    }

    @Override
    public int updateTask(Task task) {
        save();
        return super.updateTask(task);
    }

    @Override
    public int updateEpic(Epic epic) {
        save();
        return super.updateEpic(epic);
    }

    @Override
    public int updateSubtask(Subtask subtask) {
        save();
        return super.updateSubtask(subtask);
    }

    @Override
    public int removeTaskById(int id) {
        save();
        return super.removeTaskById(id);
    }

    @Override
    public int removeEpicById(int id) {
        save();
        return super.removeEpicById(id);
    }

    @Override
    public int removeSubtaskById(int id) {
        save();
        return super.removeSubtaskById(id);
    }

    @Override
    public void removeTasks() {
        save();
        super.removeTasks();
    }

    @Override
    public void removeEpics() {
        save();
        super.removeEpics();
    }

    @Override
    public void removeSubtasks() {
        save();
        super.removeSubtasks();
    }
}
