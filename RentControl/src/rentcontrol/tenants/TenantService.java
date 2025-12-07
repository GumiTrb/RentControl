package rentcontrol.tenants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rentcontrol.JsonStorage;
import rentcontrol.exceptions.EntityNotFoundException;
import rentcontrol.exceptions.ValidationException;
import rentcontrol.exceptions.StorageException;
import rentcontrol.contracts.ContractService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления арендаторами.
 * <p>
 * Реализует операции:
 * <ul>
 *     <li>создание</li>
 *     <li>редактирование</li>
 *     <li>удаление</li>
 *     <li>поиск</li>
 * </ul>
 */
public class TenantService {

    private static final Logger logger = LogManager.getLogger(TenantService.class);

    private final JsonStorage<Tenant> storage;
    private final List<Tenant> tenants;
    private final ContractService contractService;

    /**
     * Загружает арендаторов из файла tenants.json.
     */
    public TenantService(ContractService contractService) {
        this.contractService = contractService;
        this.storage = new JsonStorage<>("data/tenants.json", Tenant.class);
        try {
            this.tenants = new ArrayList<>(storage.loadAll());
            logger.info("Загружено арендаторов: {}", tenants.size());
        } catch (Exception e) {
            logger.error("Ошибка загрузки арендаторов", e);
            throw new StorageException("Ошибка загрузки файла tenants.json");
        }
    }

    /**
     * @return копия списка арендаторов
     */
    public List<Tenant> getAll() {
        return new ArrayList<>(tenants);
    }

    /**
     * Выполняет поиск по ФИО.
     *
     * @param query поисковая строка
     * @return список совпадений
     */
    public List<Tenant> search(String query) {
        if (query == null || query.isBlank()) {
            return getAll();
        }
        String q = query.toLowerCase();

        return tenants.stream()
                .filter(t -> t.getFullName() != null &&
                        t.getFullName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    /**
     * Добавляет нового арендатора.
     *
     * @throws ValidationException если ФИО пустое
     */
    public Tenant addTenant(String fullName, String phone, String email, String notes) {

        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("ФИО арендатора не может быть пустым");
        }

        Tenant t = new Tenant(fullName, phone, email, notes);
        tenants.add(t);
        save();
        logger.info("Добавлен арендатор: {} (id={})", fullName, t.getId());
        return t;
    }

    /**
     * Обновляет данные арендатора.
     *
     * @throws EntityNotFoundException если объект отсутствует
     * @throws ValidationException     если ФИО не указано
     */
    public void updateTenant(Tenant tenant,
                             String fullName,
                             String phone,
                             String email,
                             String notes) {

        Tenant existing = tenants.stream()
                .filter(t -> t.getId().equals(tenant.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Арендатор не найден — возможно, он уже был удалён."));

        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("ФИО арендатора не может быть пустым");
        }

        existing.setFullName(fullName);
        existing.setPhone(phone);
        existing.setEmail(email);
        existing.setNotes(notes);

        save();
        logger.info("Обновлён арендатор: {} (id={})", fullName, tenant.getId());
    }

    /**
     * Удаляет арендатора.
     *
     * @throws ValidationException     если передан null
     * @throws EntityNotFoundException если объект отсутствует
     */
    public void deleteTenant(Tenant tenant) {

        if (tenant == null)
            throw new ValidationException("Сначала выберите арендатора");

        // Проверяем, участвует ли в договоре
        boolean isInContract = contractService.getAll().stream()
                .anyMatch(c -> c.getTenant() != null && c.getTenant().getId().equals(tenant.getId()));

        if (isInContract)
            throw new ValidationException("Нельзя удалить арендатора — он участвует в договоре.");

        boolean removed = tenants.removeIf(t -> t.getId().equals(tenant.getId()));

        if (!removed)
            throw new EntityNotFoundException("Невозможно удалить — арендатор не найден.");

        save();
        logger.info("Удалён арендатор: {} (id={})", tenant.getFullName(), tenant.getId());
    }

    /**
     * Сохраняет список в tenants.json.
     *
     * @throws StorageException при ошибке записи
     */
    private void save() {
        try {
            storage.saveAll(tenants);
        } catch (Exception e) {
            logger.error("Ошибка сохранения арендаторов", e);
            throw new StorageException("Не удалось сохранить tenants.json");
        }
    }
}