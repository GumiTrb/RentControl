package rentcontrol.payments;

import rentcontrol.BaseEntity;
import rentcontrol.contracts.Contract;

import java.time.LocalDate;

/**
 * Модель платежа по договору аренды.
 * <p>
 * Платёж включает:
 * <ul>
 *     <li>дату платежа</li>
 *     <li>сумму</li>
 *     <li>тип (Аренда, Коммуналка, Штраф, Депозит)</li>
 *     <li>заметки</li>
 *     <li>ссылку на договор</li>
 * </ul>
 */
public class Payment extends BaseEntity {

    private Contract contract;
    private LocalDate date;
    private double amount;
    private String type;
    private String notes;

    /**
     * Пустой конструктор с генерацией ID.
     */
    public Payment() {
        super();
    }

    /**
     * Полный конструктор платежа.
     */
    public Payment(Contract contract,
                   LocalDate date,
                   double amount,
                   String type,
                   String notes) {
        this();
        this.contract = contract;
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.notes = notes;
    }

    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /**
     * Форматированное отображение платежа в списках.
     */
    @Override
    public String toString() {
        String contractInfo = "";
        if (contract != null) {
            String tenantName = contract.getTenant() != null
                    ? contract.getTenant().getFullName()
                    : "?";
            String propTitle = contract.getProperty() != null
                    ? contract.getProperty().getTitle()
                    : "?";
            contractInfo = " | " + tenantName + " / " + propTitle;
        }
        String dateText = date != null ? date.toString() : "-";
        return dateText + " • " + amount + " • " + type + contractInfo;
    }
}
