package rentcontrol.payments;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rentcontrol.JsonStorage;
import rentcontrol.contracts.Contract;
import rentcontrol.exceptions.EntityNotFoundException;
import rentcontrol.exceptions.ValidationException;
import rentcontrol.exceptions.StorageException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления платежами.
 */
public class PaymentService {

    private static final Logger logger = LogManager.getLogger(PaymentService.class);

    private final JsonStorage<Payment> storage;
    private final List<Payment> payments;

    public PaymentService() {
        this.storage = new JsonStorage<>("data/payments.json", Payment.class);
        try {
            this.payments = new ArrayList<>(storage.loadAll());
        } catch (Exception e) {
            logger.error("Ошибка загрузки платежей", e);
            throw new StorageException("Ошибка чтения payments.json");
        }
    }

    public List<Payment> getAll() {
        // сортировка по дате перед возвратом
        return payments.stream()
                .sorted(Comparator.comparing(Payment::getDate))
                .collect(Collectors.toList());
    }

    public Payment add(Contract contract,
                       java.time.LocalDate date,
                       double amount,
                       String type,
                       String notes) {

        if (date == null)
            throw new ValidationException("Дата платежа обязательна");

        if (amount <= 0)
            throw new ValidationException("Сумма должна быть больше 0");

        if (type == null || type.isBlank())
            throw new ValidationException("Выберите тип платежа");

        Payment p = new Payment(contract, date, amount, type, notes);
        payments.add(p);

        updateContractStatus(contract);
        save();

        logger.info("Добавлен платёж {} (id={})", amount, p.getId());
        return p;
    }

    public void update(Payment payment,
                       Contract contract,
                       java.time.LocalDate date,
                       double amount,
                       String type,
                       String notes) {

        Payment existing = payments.stream()
                .filter(p -> p.getId().equals(payment.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Платёж не найден — невозможно обновить"));

        if (date == null) throw new ValidationException("Дата обязательна");
        if (amount <= 0) throw new ValidationException("Сумма должна быть больше 0");
        if (type == null || type.isBlank()) throw new ValidationException("Выберите тип платежа");

        existing.setContract(contract);
        existing.setDate(date);
        existing.setAmount(amount);
        existing.setType(type);
        existing.setNotes(notes);

        updateContractStatus(contract);
        save();

        logger.info("Платёж обновлён (id={})", existing.getId());
    }

    public void delete(Payment payment) {
        if (payment == null)
            throw new ValidationException("Выберите платёж для удаления");

        boolean removed = payments.removeIf(p -> p.getId().equals(payment.getId()));

        if (!removed)
            throw new EntityNotFoundException("Такого платежа нет");

        updateContractStatus(payment.getContract());
        save();

        logger.info("Платёж удалён (id={})", payment.getId());
    }


    // ============================================================
    // === КОРРЕКТНАЯ ЛОГИКА РАСЧЁТА СТАТУСА ДОГОВОРА ============
    // ============================================================

    private void updateContractStatus(Contract contract) {
        if (contract == null) return;

        // --- если договор завершён, статус НЕ МЕНЯЕМ ---
        if ("Завершён".equalsIgnoreCase(contract.getStatus())) {
            return;
        }

        // --- если срок аренды закончился — завершить ---
        if (contract.getEndDate() != null &&
                contract.getEndDate().isBefore(java.time.LocalDate.now())) {

            contract.setStatus("Завершён");
            return;
        }

        // --- учитываем только тип "Аренда" ---
        double paid = getByContract(contract).stream()
                .filter(p -> "Аренда".equalsIgnoreCase(p.getType()))
                .mapToDouble(Payment::getAmount)
                .sum();

        double rent = contract.getMonthlyRent();

        if (paid >= rent) {
            contract.setStatus("Оплачен");
        } else {
            contract.setStatus("Долг: " + (rent - paid));
        }
    }


    public List<Payment> getByContract(Contract contract) {
        if (contract == null) return List.of();

        return payments.stream()
                .filter(p -> p.getContract() != null &&
                        p.getContract().getId().equals(contract.getId()))
                .sorted(Comparator.comparing(Payment::getDate))
                .collect(Collectors.toList());
    }


    public List<Payment> search(String query) {
        if (query == null || query.isBlank())
            return getAll();

        String q = query.toLowerCase();

        return getAll().stream()
                .filter(p ->
                        (p.getType() != null && p.getType().toLowerCase().contains(q)) ||
                                (p.getNotes() != null && p.getNotes().toLowerCase().contains(q)) ||
                                (p.getContract() != null &&
                                        (
                                                (p.getContract().getTenant() != null &&
                                                        p.getContract().getTenant().getFullName().toLowerCase().contains(q))
                                                        ||
                                                        (p.getContract().getProperty() != null &&
                                                                p.getContract().getProperty().getTitle().toLowerCase().contains(q))
                                        )
                                )
                )
                .collect(Collectors.toList());
    }


    // оставляем только расчёт аренды
    public double calculateBalance(Contract contract) {
        if (contract == null) return 0;

        double paid = getByContract(contract).stream()
                .filter(p -> "Аренда".equalsIgnoreCase(p.getType()))
                .mapToDouble(Payment::getAmount)
                .sum();

        return paid - contract.getMonthlyRent();
    }

    private void save() {
        try {
            storage.saveAll(payments);
        } catch (Exception e) {
            logger.error("Ошибка сохранения платежей", e);
            throw new StorageException("Не удалось сохранить payments.json");
        }
    }
}
