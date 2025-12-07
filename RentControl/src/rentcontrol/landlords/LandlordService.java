package rentcontrol.landlords;

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
 * Сервис управления арендодателями.
 * <p>
 * Реализует:
 * <ul>
 *     <li>Загрузку данных из JSON</li>
 *     <li>Добавление нового арендодателя</li>
 *     <li>Редактирование данных</li>
 *     <li>Удаление</li>
 *     <li>Поиск</li>
 * </ul>
 * Все изменения автоматически сохраняются в JSON-файл.
 */
public class LandlordService {

    private static final Logger logger = LogManager.getLogger(LandlordService.class);

    private final JsonStorage<Landlord> storage;
    private final List<Landlord> landlords;
    private final ContractService contractService;

    /**
     * Загружает список арендодателей из JSON.
     */
    public LandlordService(ContractService contractService) {
        this.contractService = contractService;
        this.storage = new JsonStorage<>("data/landlords.json", Landlord.class);

        try {
            this.landlords = new ArrayList<>(storage.loadAll());
            logger.info("Загружено арендодателей: {}", landlords.size());
        } catch (Exception e) {
            logger.error("Ошибка загрузки landlords.json", e);
            throw new StorageException("Ошибка загрузки файла арендодателей");
        }
    }

    /**
     * @return копия списка арендодателей
     */
    public List<Landlord> getAll() {
        return new ArrayList<>(landlords);
    }

    /**
     * Выполняет поиск по ФИО, телефону или email.
     *
     * @param query поисковый запрос
     * @return список найденных арендодателей
     */
    public List<Landlord> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        String q = query.toLowerCase();

        return landlords.stream()
                .filter(l ->
                        (l.getFullName() != null && l.getFullName().toLowerCase().contains(q)) ||
                                (l.getPhone() != null && l.getPhone().toLowerCase().contains(q)) ||
                                (l.getEmail() != null && l.getEmail().toLowerCase().contains(q))
                )
                .collect(Collectors.toList());
    }

    /**
     * Создаёт нового арендодателя.
     *
     * @throws ValidationException если ФИО пустое
     */
    public Landlord addLandlord(String fullName, String phone, String email, String notes) {

        if (fullName == null || fullName.isBlank())
            throw new ValidationException("ФИО арендодателя должно быть заполнено");

        Landlord l = new Landlord(fullName, phone, email, notes);
        landlords.add(l);
        save();

        logger.info("Добавлен арендодатель {} (id={})", fullName, l.getId());
        return l;
    }

    /**
     * Обновляет данные существующего арендодателя.
     *
     * @throws EntityNotFoundException если арендодатель отсутствует
     * @throws ValidationException если ФИО пустое
     */
    public void updateLandlord(Landlord landlord,
                               String fullName,
                               String phone,
                               String email,
                               String notes) {

        Landlord existing = landlords.stream()
                .filter(x -> x.getId().equals(landlord.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Арендодатель не найден для обновления"));

        if (fullName == null || fullName.isBlank())
            throw new ValidationException("ФИО арендодателя не может быть пустым");

        existing.setFullName(fullName);
        existing.setPhone(phone);
        existing.setEmail(email);
        existing.setNotes(notes);

        save();
        logger.info("Данные арендодателя обновлены (id={})", existing.getId());
    }

    /**
     * Удаляет арендодателя.
     *
     * @throws EntityNotFoundException если объект отсутствует
     * @throws ValidationException если с арендодателем есть договоры
     */
    public void deleteLandlord(Landlord landlord) {
        // Проверка: есть ли договоры с этим арендодателем
        boolean isInContract = contractService.getAll().stream()
                .anyMatch(c -> c.getLandlord() != null && c.getLandlord().getId().equals(landlord.getId()));

        if (isInContract)
            throw new ValidationException("Нельзя удалить арендодателя — он участвует в договоре.");

        boolean removed = landlords.removeIf(l -> l.getId().equals(landlord.getId()));

        if (!removed)
            throw new EntityNotFoundException("Арендодатель не найден — удалить невозможно.");

        save();
        logger.info("Арендодатель удалён (id={})", landlord.getId());
    }

    /**
     * Сохраняет все изменения в JSON.
     *
     * @throws StorageException при ошибке записи
     */
    private void save() {
        try {
            storage.saveAll(landlords);
        } catch (Exception e) {
            logger.error("Ошибка сохранения landlords.json", e);
            throw new StorageException("Ошибка записи файла landlords.json");
        }
    }
}