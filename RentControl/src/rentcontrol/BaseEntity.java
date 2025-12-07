package rentcontrol;

import java.util.UUID;

/**
 * Базовый абстрактный класс для всех сущностей приложения,
 * обеспечивающий наличие уникального идентификатора (UUID).
 * <p>
 * Используется как родительский класс для моделей:
 * Tenant, Landlord, Property, Contract, Payment.
 * </p>
 */
public abstract class BaseEntity {

    /** Уникальный идентификатор сущности. */
    protected String id;

    /**
     * Конструктор по умолчанию.
     * Генерирует уникальный UUID при создании объекта.
     */
    public BaseEntity() {
        this.id = UUID.randomUUID().toString();
    }

    /**
     * @return уникальный идентификатор сущности.
     */
    public String getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор сущности.
     * Обычно не используется, но оставлен для совместимости (например, при десериализации JSON).
     *
     * @param id строковый UUID
     */
    public void setId(String id) {
        this.id = id;
    }
}
