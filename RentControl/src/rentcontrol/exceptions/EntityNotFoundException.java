package rentcontrol.exceptions;

/**
 * Исключение выбрасывается, когда запрашиваемая сущность
 * отсутствует в хранилище или коллекции.
 */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
