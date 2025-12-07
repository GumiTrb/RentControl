package rentcontrol.properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rentcontrol.JsonStorage;
import rentcontrol.exceptions.ValidationException;
import rentcontrol.exceptions.EntityNotFoundException;
import rentcontrol.exceptions.StorageException;
import rentcontrol.contracts.ContractService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления объектами недвижимости.
 * <p>
 * Реализует:
 * <ul>
 *     <li>добавление объекта</li>
 *     <li>редактирование</li>
 *     <li>удаление</li>
 *     <li>поиск по названию и адресу</li>
 * </ul>
 * Все изменения автоматически сохраняются в JSON.
 */
public class PropertyService {

    private static final Logger logger = LogManager.getLogger(PropertyService.class);

    private final JsonStorage<Property> storage;
    private final List<Property> properties;
    private final ContractService contractService;

    /**
     * Загружает объекты недвижимости из properties.json.
     */
    public PropertyService(ContractService contractService) {
        this.contractService = contractService;
        this.storage = new JsonStorage<>("data/properties.json", Property.class);

        try {
            this.properties = new ArrayList<>(storage.loadAll());
            logger.info("Загружено объектов: {}", properties.size());
        } catch (Exception e) {
            logger.error("Ошибка чтения properties.json", e);
            throw new StorageException("Ошибка чтения файла объектов");
        }
    }

    /**
     * @return копия списка объектов
     */
    public List<Property> getAll() {
        return new ArrayList<>(properties);
    }

    /**
     * Поиск по названию или адресу.
     *
     * @param query поисковый запрос
     * @return найденные объекты
     */
    public List<Property> search(String query) {
        if (query == null || query.isBlank()) return getAll();

        String q = query.toLowerCase();

        return properties.stream()
                .filter(p ->
                        (p.getTitle() != null && p.getTitle().toLowerCase().contains(q)) ||
                                (p.getAddress() != null && p.getAddress().toLowerCase().contains(q))
                )
                .collect(Collectors.toList());
    }

    /**
     * Создаёт новый объект недвижимости.
     *
     * @throws ValidationException если площадь или цена некорректны
     */
    public Property add(String title, String addr, double area, double price, String notes) {

        if (title == null || title.isBlank())
            throw new ValidationException("Название объекта не может быть пустым");

        if (price <= 0)
            throw new ValidationException("Цена аренды должна быть больше 0");

        if (area <= 0)
            throw new ValidationException("Площадь должна быть больше нуля");

        Property p = new Property(title, addr, area, price, notes);
        properties.add(p);
        save();

        logger.info("Добавлен объект недвижимости {} (id={})", title, p.getId());
        return p;
    }

    /**
     * Обновляет объект.
     *
     * @throws EntityNotFoundException если объект отсутствует
     */
    public void update(Property p, String title, String addr, double area, double price, String notes) {

        Property existing = properties.stream()
                .filter(x -> x.getId().equals(p.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Объект не найден — обновление невозможно"));

        if (title == null || title.isBlank())
            throw new ValidationException("Название объекта не может быть пустым");

        if (price <= 0)
            throw new ValidationException("Цена должна быть положительной");

        if (area <= 0)
            throw new ValidationException("Площадь должна быть больше нуля");

        existing.setTitle(title);
        existing.setAddress(addr);
        existing.setArea(area);
        existing.setPrice(price);
        existing.setNotes(notes);

        save();
        logger.info("Объект обновлён (id={})", existing.getId());
    }

    /**
     * Удаляет объект недвижимости.
     *
     * @throws EntityNotFoundException если объект отсутствует
     */
    public void delete(Property p) {
        // Проверка: есть ли договоры с этим объектом недвижимости
        boolean isInContract = contractService.getAll().stream()
                .anyMatch(c -> c.getProperty() != null && c.getProperty().getId().equals(p.getId()));

        if (isInContract)
            throw new ValidationException("Нельзя удалить объект — он участвует в договоре.");

        boolean removed = properties.removeIf(x -> x.getId().equals(p.getId()));

        if (!removed)
            throw new EntityNotFoundException("Объект не найден — удалить невозможно");

        save();
        logger.info("Объект недвижимости удалён (id={})", p.getId());
    }

    /**
     * Сохраняет изменения в JSON-файл.
     *
     * @throws StorageException при ошибке записи
     */
    private void save() {
        try {
            storage.saveAll(properties);
        } catch (Exception e) {
            logger.error("Ошибка сохранения properties.json", e);
            throw new StorageException("Ошибка записи файла объектов");
        }
    }
}