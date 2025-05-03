package service;

import service.exception.ManagerSaveException;
import task.*;

import java.io.*;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private File file;

    public FileBackedTaskManager(File file) {
        if (file == null || !file.exists()) {
            handleUserFileChoice();
        }
        this.file = file;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic,localDateTime,duration\n");
            writeTasks(writer, getAllTask());
            writeTasks(writer, getAllEpic());
            writeTasks(writer, getAllSubtask());

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла");
        }
    }

    private File handleUserFileChoice() {
        System.out.println("Файл не найден или отсутствует. Вы хотите продолжить работу программы?" + "\n" +
                "Введите ( 'y' / 'n' ) при соответствующем выборе: ");
        Scanner s = new Scanner(System.in);
        String choice = s.nextLine().trim().toLowerCase();
        return switch (choice) {
            case "y" -> {
                this.file = createFile();
                yield file;
            }
            case "n" -> throw new IllegalArgumentException("Файл отсутствует или не найден. Работа прекращена");
            default -> {
                System.out.println("Некорректный ввод. Пожалуйста, введите 'y' или 'n'. ");
                yield handleUserFileChoice();
            }
        };
    }

    private File createFile() {
        Scanner s = new Scanner(System.in);
        System.out.println("Введите желаемое название файла: ");
        String fileName = s.nextLine();
        System.out.println("Введите желаемый путь для файла: ");
        String path = s.nextLine();
        File file = Paths.get(path).resolve(fileName).toFile();

        if (!file.exists()) {
            System.out.println("Указанный путь не существует. Введите заново соответствующие параметры.");
            createFile();
        }

        try {
            if (file.createNewFile()) {
                System.out.println("Файл успешно создан");
                return file;
            } else {
                System.out.println("Файл с таким именем уже существует");
                return createFile();
            }
        } catch (IOException e) {
            System.out.println("Ошибка при создании файла");
            throw new RuntimeException(e);
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
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

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

    private static Task parseTaskFromString(String line) {
        String[] arrayString = line.split(",");

        int id = Integer.parseInt(arrayString[0]);
        TaskStatus taskStatus = TaskStatus.valueOf(arrayString[1]);
        String name = arrayString[2];
        String description = arrayString[3];
        Status status = Status.valueOf(arrayString[4]);
        LocalDateTime localDateTime = LocalDateTime.parse(arrayString[5]);
        Duration duration = Duration.parse(arrayString[6]);

        switch (taskStatus) {
            case TASK:
                return new Task(id, name, description, status, localDateTime, duration);
            case EPIC:
                return new Epic(id, name, description, status, localDateTime, duration);
            case SUBTASK: {
                int epicId = Integer.parseInt(arrayString[7]);
                return new Subtask(id, name, description, status, localDateTime, duration, epicId);
            }
            default:
                System.out.println("Неизвестная задача");
        }
        return null;
    }

    @Override
    public int createEpic(Epic epic) {
        int result = super.createEpic(epic);
        if (result > 0) {
            save();
        }
        return result;
    }

    @Override
    public int createTask(Task task) {
        int result = super.createTask(task);
        if (result > 0) {
            save();
        }
        return result;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        int result = super.createSubtask(subtask);
        if (result > 0) {
            save();
        }
        return result;
    }

    @Override
    public int updateTask(Task task) {
        int result = super.updateTask(task);
        if (result > 0) {
            save();
        }
        return result;
    }

    @Override
    public int updateEpic(Epic epic) {
        int result = super.updateEpic(epic);
        if (result > 0) {
            save();
        }
        return result;
    }

    @Override
    public int updateSubtask(Subtask subtask) {
        int result = super.updateSubtask(subtask);
        if (result > 0) {
            save();
        }
        return result;
    }

    @Override
    public int removeTaskById(int id) {
        int result = super.removeTaskById(id);
        if (result > 0) {
            save();
        }
        return result;
    }

    @Override
    public int removeEpicById(int id) {
        int result = super.removeEpicById(id);
        if (result > 0) {
            save();
        }
        return result;
    }

    @Override
    public int removeSubtaskById(int id) {
        int result = super.removeSubtaskById(id);
        if (result > 0) {
            save();
        }
        return result;
    }

    @Override
    public void removeTasks() {
        super.removeTasks();
        save();
    }

    @Override
    public void removeEpics() {
        super.removeEpics();
        save();
    }

    @Override
    public void removeSubtasks() {
        super.removeSubtasks();
        save();
    }
}
