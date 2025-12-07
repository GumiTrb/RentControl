package rentcontrol.contracts;

import rentcontrol.BaseEntity;
import rentcontrol.tenants.Tenant;
import rentcontrol.landlords.Landlord;
import rentcontrol.properties.Property;

import java.time.LocalDate;

/**
 * Представляет договор аренды между арендатором, арендодателем и объектом недвижимости.
 * <p>
 * Договор включает:
 * <ul>
 *     <li>Участников договора (арендатор, арендодатель)</li>
 *     <li>Объект недвижимости</li>
 *     <li>Период действия договора</li>
 *     <li>Ежемесячную арендную плату</li>
 *     <li>Статус ("Активен", "Завершён")</li>
 * </ul>
 * Класс наследуется от {@link BaseEntity}, что обеспечивает уникальный идентификатор.
 */
public class Contract extends BaseEntity {

    private Tenant tenant;
    private Landlord landlord;
    private Property property;

    private LocalDate startDate;
    private LocalDate endDate;

    private double monthlyRent;

    private String status; // Активен / Завершён

    /**
     * Пустой конструктор. Генерирует новый уникальный ID через {@code BaseEntity}.
     */
    public Contract() {
        super();
    }

    /**
     * Создаёт новый договор аренды.
     *
     * @param t     арендатор
     * @param l     арендодатель
     * @param p     объект недвижимости
     * @param start дата начала договора
     * @param end   дата завершения договора
     * @param rent  ежемесячная аренда
     */
    public Contract(Tenant t, Landlord l, Property p,
                    LocalDate start, LocalDate end, double rent) {
        this();
        this.tenant = t;
        this.landlord = l;
        this.property = p;
        this.startDate = start;
        this.endDate = end;
        this.monthlyRent = rent;
        this.status = "Активен";
    }

    public Tenant getTenant() { return tenant; }
    public Landlord getLandlord() { return landlord; }
    public Property getProperty() { return property; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getMonthlyRent() { return monthlyRent; }
    public String getStatus() { return status; }

    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public void setLandlord(Landlord landlord) { this.landlord = landlord; }
    public void setProperty(Property property) { this.property = property; }
    public void setStartDate(LocalDate s) { this.startDate = s; }
    public void setEndDate(LocalDate e) { this.endDate = e; }
    public void setMonthlyRent(double r) { this.monthlyRent = r; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Представление договора в виде строки — используется в списках.
     *
     * @return ФИО арендатора + название объекта
     */
    @Override
    public String toString() {
        return tenant.getFullName() + " — " + property.getTitle();
    }
}
