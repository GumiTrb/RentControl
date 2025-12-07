package rentcontrol.landlords;

import rentcontrol.BaseEntity;

/**
 * Модель арендодателя.
 * <p>
 * Содержит основную контактную информацию:
 * <ul>
 *     <li>ФИО</li>
 *     <li>Телефон</li>
 *     <li>Email</li>
 *     <li>Заметки</li>
 * </ul>
 * Наследует уникальный идентификатор от {@link BaseEntity}.
 */
public class Landlord extends BaseEntity {

    private String fullName;
    private String phone;
    private String email;
    private String notes;

    /**
     * Пустой конструктор, генерирующий уникальный ID.
     */
    public Landlord() {
        super();
    }

    /**
     * Создаёт нового арендодателя.
     *
     * @param fullName ФИО арендодателя
     * @param phone    номер телефона
     * @param email    электронная почта
     * @param notes    дополнительные заметки
     */
    public Landlord(String fullName, String phone, String email, String notes) {
        this();
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /**
     * Представление для отображения в списках.
     */
    @Override
    public String toString() {
        return fullName;
    }
}
