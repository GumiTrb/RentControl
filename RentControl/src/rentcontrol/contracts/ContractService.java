package rentcontrol.contracts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rentcontrol.JsonStorage;
import rentcontrol.exceptions.EntityNotFoundException;
import rentcontrol.exceptions.ValidationException;
import rentcontrol.exceptions.StorageException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис управления договорами аренды.
 * <p>
 * Реализует:
 * <ul>
 *     <li>Загрузку данных из JSON-хранилища</li>
 *     <li>CRUD-операции (создание, редактирование, удаление)</li>
 *     <li>Валидацию данных</li>
 *     <li>Логирование всех действий</li>
 * </ul>
 */
public class ContractService {

    private static final Logger logger = LogManager.getLogger(ContractService.class);

    private final JsonStorage<Contract> storage;
    private final List<Contract> contracts;

    /**
     * Загружает список договоров из JSON-файла.
     * Если загрузка невозможна, выбрасывает {@link StorageException}.
     */
    public ContractService() {
        this.storage = new JsonStorage<>("data/contracts.json", Contract.class);
        try {
            this.contracts = new ArrayList<>(storage.loadAll());
            logger.info("Загружено договоров: {}", contracts.size());

            // Автоматическое обновление статуса завершённых договоров
            for (Contract c : contracts) {
                updateStatus(c);
            }
            save();

        } catch (Exception e) {
            logger.error("Ошибка чтения contracts.json", e);
            throw new StorageException("Не удалось загрузить список договоров");
        }
    }

    /**
     * Обновляет статус договора на основе даты окончания.
     */
    public void updateStatus(Contract c) {
        if (c.getEndDate() != null && c.getEndDate().isBefore(LocalDate.now())) {
            c.setStatus("Завершён");
        }
    }

    /**
     * @return копия списка всех договоров
     */
    public List<Contract> getAll() {
        return new ArrayList<>(contracts);
    }

    /**
     * Добавляет новый договор.
     *
     * @param c договор для сохранения
     * @return сохранённый договор
     * @throws ValidationException если договор не содержит обязательных полей
     */
    public Contract add(Contract c) {
        if (c.getTenant() == null)
            throw new ValidationException("У договора должен быть арендатор");

        if (c.getLandlord() == null)
            throw new ValidationException("У договора должен быть арендодатель");

        if (c.getProperty() == null)
            throw new ValidationException("У договора должен быть объект");

        updateStatus(c);

        contracts.add(c);
        save();

        logger.info("Создан договор {}", c.getId());
        return c;
    }

    /**
     * Обновляет существующий договор.
     *
     * @param old     договор, который следует обновить
     * @param updated объект с новыми значениями
     * @throws EntityNotFoundException если договор отсутствует в списке
     * @throws ValidationException     если обязательные поля отсутствуют
     */
    public void update(Contract old, Contract updated) {

        Contract existing = contracts.stream()
                .filter(x -> x.getId().equals(old.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Договор не найден — возможно, он был удалён"));

        if (updated.getTenant() == null)
            throw new ValidationException("У договора должен быть арендатор");

        if (updated.getLandlord() == null)
            throw new ValidationException("У договора должен быть арендодатель");

        if (updated.getProperty() == null)
            throw new ValidationException("У договора должен быть объект");

        existing.setTenant(updated.getTenant());
        existing.setLandlord(updated.getLandlord());
        existing.setProperty(updated.getProperty());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setMonthlyRent(updated.getMonthlyRent());
        existing.setStatus(updated.getStatus());

        updateStatus(existing);

        save();
        logger.info("Обновлён договор {}", existing.getId());
    }

    /**
     * Удаляет договор по ID.
     *
     * @param c договор для удаления
     * @throws EntityNotFoundException если договор не найден
     */
    public void delete(Contract c) {

        boolean removed = contracts.removeIf(x -> x.getId().equals(c.getId()));

        if (!removed)
            throw new EntityNotFoundException("Нельзя удалить — договор не найден.");

        save();
        logger.info("Удалён договор {}", c.getId());
    }

    /**
     * Сохраняет текущее состояние списка договоров в JSON.
     *
     * @throws StorageException если сохранение невозможно
     */
    private void save() {
        try {
            storage.saveAll(contracts);
        } catch (Exception e) {
            logger.error("Ошибка сохранения contracts.json", e);
            throw new StorageException("Не удалось сохранить список договоров");
        }
    }
}