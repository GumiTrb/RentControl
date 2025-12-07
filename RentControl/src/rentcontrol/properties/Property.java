package rentcontrol.properties;

import rentcontrol.BaseEntity;

/**
 * Модель объекта недвижимости.
 * <p>
 * Содержит данные:
 * <ul>
 *     <li>название</li>
 *     <li>адрес</li>
 *     <li>площадь</li>
 *     <li>стоимость аренды в месяц</li>
 *     <li>заметки</li>
 * </ul>
 * Наследует уникальный ID от {@link BaseEntity}.
 */
public class Property extends BaseEntity {

    private String title;
    private String address;
    private double area;
    private double price;
    private String notes;

    /**
     * Пустой конструктор.
     */
    public Property() {
        super();
    }

    /**
     * Полный конструктор объекта недвижимости.
     */
    public Property(String title, String address, double area, double price, String notes) {
        this();
        this.title = title;
        this.address = address;
        this.area = area;
        this.price = price;
        this.notes = notes;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /**
     * Формат вывода в списках UI.
     */
    @Override
    public String toString() {
        return title + " (" + address + ")";
    }
}
