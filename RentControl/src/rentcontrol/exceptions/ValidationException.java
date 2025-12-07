package rentcontrol.exceptions;

/**
 * Ошибка пользовательской валидации.
 * Выбрасывается при некорректных или отсутствующих данных.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
