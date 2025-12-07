package rentcontrol.tenants;

import rentcontrol.BaseEntity;

/**
 * Модель арендатора.
 */
public class Tenant extends BaseEntity {

    private String fullName;
    private String phone;
    private String email;
    private String notes;

    public Tenant() {
        super();
    }

    public Tenant(String fullName, String phone, String email, String notes) {
        this();
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        // что будет отображаться в ListView
        return fullName;
    }
}
