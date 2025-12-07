package rentcontrol.exceptions;

/**
 * Ошибка работы с файловым хранилищем.
 * Используется при сбоях чтения и записи JSON.
 */
public class StorageException extends RuntimeException {

    /**
     * Полный конструктор с вложенной причиной ошибки.
     *
     * @param message описание ошибки
     * @param cause   исходная причина
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Конструктор без вложенной ошибки.
     *
     * @param message описание ошибки
     */
    public StorageException(String message) {
        super(message);
    }
}
