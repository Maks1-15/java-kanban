package service;

// Создали собственное исключение для вывода ошибки в методе save
public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String message) {
        super(message);
    }
}
