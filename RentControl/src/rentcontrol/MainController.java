package rentcontrol;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rentcontrol.contracts.Contract;
import rentcontrol.contracts.ContractService;
import rentcontrol.exceptions.ValidationException;
import rentcontrol.exceptions.StorageException;
import rentcontrol.landlords.Landlord;
import rentcontrol.landlords.LandlordService;
import rentcontrol.payments.Payment;
import rentcontrol.payments.PaymentService;
import rentcontrol.properties.Property;
import rentcontrol.properties.PropertyService;
import rentcontrol.tenants.Tenant;
import rentcontrol.tenants.TenantService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Главный контроллер приложения управления арендой недвижимости.
 * Отвечает за управление пользовательским интерфейсом, обработку событий
 * и координацию работы всех бизнес-сущностей системы.
 *
 * <p>Контроллер реализует следующие функции:
 * <ul>
 *   <li>Управление арендаторами (CRUD операции)</li>
 *   <li>Управление арендодателями (CRUD операции)</li>
 *   <li>Управление объектов недвижимости (CRUD операции)</li>
 *   <li>Управление договорами аренды (CRUD операции)</li>
 *   <li>Управление платежей (CRUD операции)</li>
 *   <li>Расчет графика платежей с учетом частичных месяцев</li>
 *   <li>Поиск и фильтрация данных</li>
 *   <li>Навигация между разделами приложения</li>
 * </ul>
 *
 * <p>Контроллер использует паттерн MVC (Model-View-Controller) и взаимодействует
 * с соответствующими сервисными классами для доступа к данным.
 *
 * @author rentcontrol
 * @version 1.0
 * @since 2024
 */
public class MainController {

    /** Логгер для записи событий и ошибок приложения. */
    private static final Logger logger = LogManager.getLogger(MainController.class);

    // ==== Основные панели пользовательского интерфейса ====
    @FXML private StackPane centerPane;
    @FXML private VBox tenantsPane;
    @FXML private VBox landlordsPane;
    @FXML private VBox propertiesPane;
    @FXML private VBox contractsPane;
    @FXML private VBox paymentsPane;
    @FXML private VBox calculatorPane;

    // ==== Элементы меню для подсветки ====
    @FXML private Node menuTenants;
    @FXML private Node menuLandlords;
    @FXML private Node menuProperties;
    @FXML private Node menuContracts;
    @FXML private Node menuPayments;
    @FXML private Node menuCalculator;

    // ==== Компоненты для управления арендаторами ====
    @FXML private ListView<Tenant> tenantListView;
    @FXML private TextField searchField;
    @FXML private Label detailsPlaceholder;
    @FXML private VBox detailsBox;
    @FXML private Label lblTenantName;
    @FXML private Label lblTenantPhone;
    @FXML private Label lblTenantEmail;
    @FXML private Label lblTenantNotes;
    private TenantService tenantService;
    private ObservableList<Tenant> tenantObservableList;

    // ==== Компоненты для управления арендодателями ====
    @FXML private ListView<Landlord> landlordListView;
    @FXML private TextField searchFieldLandlords;
    @FXML private Label detailsLandlordPlaceholder;
    @FXML private VBox detailsLandlordBox;
    @FXML private Label lblLandlordName;
    @FXML private Label lblLandlordPhone;
    @FXML private Label lblLandlordEmail;
    @FXML private Label lblLandlordNotes;
    private LandlordService landlordService;
    private ObservableList<Landlord> landlordObservableList;

    // ==== Компоненты для управления объектами недвижимости ====
    @FXML private ListView<Property> propertyListView;
    @FXML private TextField searchFieldProperties;
    @FXML private Label detailsPropertyPlaceholder;
    @FXML private VBox detailsPropertyBox;
    @FXML private Label lblPropertyTitle;
    @FXML private Label lblPropertyAddress;
    @FXML private Label lblPropertyArea;
    @FXML private Label lblPropertyPrice;
    @FXML private Label lblPropertyNotes;
    private PropertyService propertyService;
    private ObservableList<Property> propertyObservableList;

    // ==== Компоненты для управления договорами ====
    @FXML private ListView<Contract> contractListView;
    @FXML private TextField searchFieldContracts;
    @FXML private ComboBox<String> contractsFilterCombo;
    @FXML private Label detailsContractPlaceholder;
    @FXML private VBox detailsContractBox;
    @FXML private Label lblContractTenant;
    @FXML private Label lblContractLandlord;
    @FXML private Label lblContractProperty;
    @FXML private Label lblContractPeriod;
    @FXML private Label lblContractRent;
    @FXML private Label lblContractStatus;
    private ContractService contractService;
    private ObservableList<Contract> contractObservableList;

    // ==== Компоненты для управления платежами ====
    @FXML private ListView<Payment> paymentListView;
    @FXML private TextField searchFieldPayments;
    @FXML private Label detailsPaymentPlaceholder;
    @FXML private VBox detailsPaymentBox;
    @FXML private Label lblPaymentDate;
    @FXML private Label lblPaymentAmount;
    @FXML private Label lblPaymentType;
    @FXML private Label lblPaymentContract;
    @FXML private Label lblPaymentNotes;
    @FXML private Label lblPaymentsCount;
    private PaymentService paymentService;
    private ObservableList<Payment> paymentObservableList;

    // ==== Компоненты для калькулятора платежей ====
    @FXML private ComboBox<Property> calcPropertyBox;
    @FXML private DatePicker calcStartPicker;
    @FXML private DatePicker calcEndPicker;
    @FXML private TextField calcRentField;
    @FXML private Label lblCalcPeriod;
    @FXML private Label lblCalcMonths;
    @FXML private Label lblCalcTotal;
    @FXML private TableView<ScheduleRow> calcTable;
    @FXML private TableColumn<ScheduleRow, String> colCalcMonth;
    @FXML private TableColumn<ScheduleRow, String> colCalcPeriod;
    @FXML private TableColumn<ScheduleRow, String> colCalcDays;
    @FXML private TableColumn<ScheduleRow, String> colCalcAmount;
    private ObservableList<ScheduleRow> calcObservableList;

    /** Форматтер для отображения дат в формате "dd.MM.yyyy". */
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // ===========================================================
    // ======================= INITIALIZE ========================
    // ===========================================================

    /**
     * Метод инициализации контроллера, вызываемый автоматически после загрузки FXML.
     * Выполняет следующие действия:
     * <ul>
     *   <li>Инициализация сервисных классов</li>
     *   <li>Настройка пользовательского интерфейса</li>
     *   <li>Настройка слушателей событий</li>
     *   <li>Загрузка начальных данных</li>
     * </ul>
     *
     * @throws RuntimeException если произошла ошибка при инициализации компонентов
     */
    @FXML
    private void initialize() {
        try {
            // Инициализация сервисов с логированием ошибок
            contractService = new ContractService();
            tenantService = new TenantService(contractService);
            landlordService = new LandlordService(contractService);
            propertyService = new PropertyService(contractService);
            paymentService = new PaymentService();
        } catch (Exception e) {
            handleException(e);
        }

        // --- Инициализация раздела арендаторов ---
        tenantObservableList = FXCollections.observableArrayList(tenantService.getAll());
        tenantListView.setItems(tenantObservableList);
        lblTenantNotes.setWrapText(true);
        tenantListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> showTenantDetails(n));
        searchField.textProperty().addListener((obs, o, n) -> applyTenantSearch(n));

        // --- Инициализация раздела арендодателей ---
        landlordObservableList = FXCollections.observableArrayList(landlordService.getAll());
        landlordListView.setItems(landlordObservableList);
        lblLandlordNotes.setWrapText(true);
        landlordListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> showLandlordDetails(n));
        searchFieldLandlords.textProperty().addListener((obs, o, n) -> applyLandlordSearch(n));

        // --- Инициализация раздела объектов недвижимости ---
        propertyObservableList = FXCollections.observableArrayList(propertyService.getAll());
        propertyListView.setItems(propertyObservableList);
        lblPropertyNotes.setWrapText(true);
        propertyListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> showPropertyDetails(n));
        searchFieldProperties.textProperty().addListener((obs, o, n) -> applyPropertySearch(n));

        // --- Инициализация раздела договоров ---
        contractObservableList = FXCollections.observableArrayList(contractService.getAll());
        contractListView.setItems(contractObservableList);
        lblContractStatus.setWrapText(true);
        contractListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> showContractDetails(n));
        contractsFilterCombo.setItems(FXCollections.observableArrayList("Все", "Активен", "Завершён"));
        contractsFilterCombo.setValue("Все");
        searchFieldContracts.textProperty().addListener((obs, o, n) -> applyContractsFilter());
        contractsFilterCombo.valueProperty().addListener((obs, o, n) -> applyContractsFilter());

        // --- Инициализация раздела платежей ---
        paymentObservableList = FXCollections.observableArrayList(paymentService.getAll());
        paymentListView.setItems(paymentObservableList);
        lblPaymentNotes.setWrapText(true);
        paymentListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> showPaymentDetails(n));
        searchFieldPayments.textProperty().addListener((obs, o, n) -> applyPaymentSearch(n));

        // --- Инициализация калькулятора ---
        if (calcTable != null) {
            calcObservableList = FXCollections.observableArrayList();
            calcTable.setItems(calcObservableList);
            colCalcMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
            colCalcPeriod.setCellValueFactory(new PropertyValueFactory<>("period"));
            colCalcDays.setCellValueFactory(new PropertyValueFactory<>("days"));
            colCalcAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        }

        if (calcPropertyBox != null) {
            calcPropertyBox.setItems(FXCollections.observableArrayList(propertyService.getAll()));
            calcPropertyBox.valueProperty().addListener((obs, o, n) -> {
                if (n != null && calcRentField.getText().isBlank()) {
                    calcRentField.setText(String.valueOf(n.getPrice()));
                }
            });
        }

        openTenants();
        logger.info("MainController инициализирован");
    }

    // ===========================================================
    // ================== ПОДСВЕТКА МЕНЮ =========================
    // ===========================================================

    /**
     * Подсвечивает активный пункт меню.
     *
     * @param id идентификатор вкладки для подсветки
     */
    private void highlightMenu(String id) {
        List<Node> items = List.of(menuTenants, menuLandlords, menuProperties, menuContracts, menuPayments, menuCalculator);
        for (Node n : items) n.getStyleClass().remove("active");

        switch (id) {
            case "tenants": menuTenants.getStyleClass().add("active"); break;
            case "landlords": menuLandlords.getStyleClass().add("active"); break;
            case "properties": menuProperties.getStyleClass().add("active"); break;
            case "contracts": menuContracts.getStyleClass().add("active"); break;
            case "payments": menuPayments.getStyleClass().add("active"); break;
            case "calculator": menuCalculator.getStyleClass().add("active"); break;
        }
    }

    // ===========================================================
    // ================ ПЕРЕКЛЮЧЕНИЕ ВКЛАДОК =====================
    // ===========================================================

    /**
     * Скрывает все панели интерфейса.
     * Используется для переключения между разделами приложения.
     */
    private void setAllInvisible() {
        tenantsPane.setVisible(false);
        landlordsPane.setVisible(false);
        propertiesPane.setVisible(false);
        contractsPane.setVisible(false);
        paymentsPane.setVisible(false);
        if (calculatorPane != null) calculatorPane.setVisible(false);
    }

    /**
     * Открывает раздел управления арендаторами.
     */
    @FXML
    private void openTenants() {
        highlightMenu("tenants");
        setAllInvisible();
        tenantsPane.setVisible(true);
        centerPane.getChildren().setAll(tenantsPane);
    }

    /**
     * Открывает раздел управления арендодателями.
     */
    @FXML
    private void openLandlords() {
        highlightMenu("landlords");
        setAllInvisible();
        landlordsPane.setVisible(true);
        centerPane.getChildren().setAll(landlordsPane);
    }

    /**
     * Открывает раздел управления объектами недвижимости.
     */
    @FXML
    private void openProperties() {
        highlightMenu("properties");
        setAllInvisible();
        propertiesPane.setVisible(true);
        centerPane.getChildren().setAll(propertiesPane);
    }

    /**
     * Открывает раздел управления договорами аренды.
     */
    @FXML
    private void openContracts() {
        highlightMenu("contracts");
        setAllInvisible();
        contractsPane.setVisible(true);
        centerPane.getChildren().setAll(contractsPane);
    }

    /**
     * Открывает раздел управления платежами.
     */
    @FXML
    private void openPayments() {
        highlightMenu("payments");
        setAllInvisible();
        paymentsPane.setVisible(true);
        centerPane.getChildren().setAll(paymentsPane);
    }

    /**
     * Открывает раздел калькулятора платежей.
     */
    @FXML
    private void openCalculator() {
        highlightMenu("calculator");
        setAllInvisible();
        calculatorPane.setVisible(true);
        centerPane.getChildren().setAll(calculatorPane);
    }

    // ===========================================================
    // ======================= ВАЛИДАТОРЫ ========================
    // ===========================================================

    /** Проверка строкового поля: только буквы, пробелы и дефисы */
    private void validateName(String value, String fieldName) {
        if (value == null || value.isBlank())
            throw new ValidationException(fieldName + " не может быть пустым");

        if (!value.matches("[A-Za-zА-Яа-яЁё\\- ]+"))
            throw new ValidationException(fieldName + " должно содержать только буквы");
    }

    /** Проверка телефона */
    private void validatePhone(String phone) {
        if (phone == null || phone.isBlank()) return; // телефон необязательный

        if (!phone.matches("[0-9+\\- ]+"))
            throw new ValidationException("Телефон может содержать только цифры, +, -, пробелы");
    }

    /** Проверка email */
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) return;

        if (!email.contains("@") || !email.contains("."))
            throw new ValidationException("Некорректный email");
    }

    /**
     * Возвращает безопасное представление строки.
     * Если строка null или пустая, возвращает дефис ("-").
     *
     * @param s исходная строка
     * @return безопасное представление строки
     */
    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    /** Проверяет корректность текста — должен содержать буквы */
    private String validateText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " не может быть пустым");
        }

        String trimmed = value.trim();

        // Не должен состоять только из цифр
        if (trimmed.matches("\\d+")) {
            throw new ValidationException(fieldName + " не может содержать только цифры");
        }

        // Должна быть хотя бы одна буква
        if (!trimmed.matches(".*[A-Za-zА-Яа-я].*")) {
            throw new ValidationException(fieldName + " должно содержать буквы");
        }

        return trimmed;
    }

    /** Проверяет корректность адреса объекта */
    private String validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new ValidationException("Адрес не может быть пустым");
        }

        String trimmed = address.trim();

        // Адрес не должен быть только цифрами
        if (trimmed.matches("\\d+")) {
            throw new ValidationException("Адрес должен содержать не только цифры");
        }

        // Разрешённые символы: буквы, цифры, пробел, дефис, слэш
        if (!trimmed.matches("[A-Za-zА-Яа-я0-9\\-\\s/]+")) {
            throw new ValidationException("Адрес содержит недопустимые символы");
        }

        if (trimmed.length() < 3) {
            throw new ValidationException("Адрес слишком короткий");
        }

        return trimmed;
    }

    /** Проверяет корректность числа (площадь, цена) */
    private double validateDouble(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " не может быть пустым");
        }

        try {
            double number = Double.parseDouble(value.trim());
            if (number <= 0) {
                throw new ValidationException(fieldName + " должно быть положительным числом");
            }
            return number;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " должно быть числом");
        }
    }

    /**
     * Загружает список арендаторов из сервиса и обновляет интерфейс.
     * В случае ошибки хранения данных выводит сообщение пользователю.
     */
    private void loadTenants() {
        try {
            tenantObservableList.setAll(tenantService.getAll());
            tenantListView.refresh();
            if (tenantListView.getSelectionModel().getSelectedItem() == null) {
                clearTenantDetails();
            }
        } catch (StorageException e) {
            handleException(e);
        }
    }

    /**
     * Применяет поисковый запрос к списку арендаторов.
     *
     * @param query поисковый запрос для фильтрации арендаторов
     */
    private void applyTenantSearch(String query) {
        try {
            tenantObservableList.setAll(tenantService.search(query));
            tenantListView.refresh();
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Отображает детальную информацию о выбранном арендаторе.
     *
     * @param tenant арендатор для отображения, если null - очищает детали
     */
    private void showTenantDetails(Tenant tenant) {
        if (tenant == null) {
            clearTenantDetails();
            return;
        }
        detailsPlaceholder.setVisible(false);
        detailsBox.setVisible(true);
        lblTenantName.setText(tenant.getFullName());
        lblTenantPhone.setText("Телефон: " + safe(tenant.getPhone()));
        lblTenantEmail.setText("Email: " + safe(tenant.getEmail()));
        lblTenantNotes.setText("Заметки: " + safe(tenant.getNotes()));
    }

    /**
     * Очищает панель с детальной информацией об арендаторе.
     */
    private void clearTenantDetails() {
        detailsPlaceholder.setVisible(true);
        detailsBox.setVisible(false);
        lblTenantName.setText("");
        lblTenantPhone.setText("");
        lblTenantEmail.setText("");
        lblTenantNotes.setText("");
    }

    /**
     * Обработчик события добавления нового арендатора.
     * Открывает диалоговое окно для ввода данных арендатора.
     */
    @FXML
    private void onAddTenant() {
        Dialog<Tenant> dialog = createTenantDialog("Добавить арендатора", null);
        dialog.showAndWait().ifPresent(t -> {
            try {
                tenantService.addTenant(
                        t.getFullName(),
                        t.getPhone(),
                        t.getEmail(),
                        t.getNotes()
                );
                loadTenants();
                logger.info("Арендатор добавлен: {}", t.getFullName());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        });
    }

    /**
     * Обработчик события редактирования выбранного арендатора.
     * Открывает диалоговое окно с текущими данными арендатора.
     */
    @FXML
    private void onEditTenant() {
        Tenant selected = tenantListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите арендатора");
            return;
        }

        Dialog<Tenant> dialog = createTenantDialog("Редактировать арендатора", selected);
        dialog.showAndWait().ifPresent(updated -> {
            try {
                tenantService.updateTenant(
                        selected,
                        updated.getFullName(),
                        updated.getPhone(),
                        updated.getEmail(),
                        updated.getNotes()
                );
                tenantListView.refresh();
                showTenantDetails(selected);
                logger.info("Арендатор обновлён: {}", selected.getFullName());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        });
    }

    /**
     * Обработчик события удаления выбранного арендатора.
     * Запрашивает подтверждение перед удалением.
     */
    @FXML
    private void onDeleteTenant() {
        Tenant selected = tenantListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите арендатора");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить арендатора?\n" + selected.getFullName(),
                ButtonType.OK, ButtonType.CANCEL);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                tenantService.deleteTenant(selected);
                loadTenants();
                logger.info("Арендатор удалён: {}", selected.getFullName());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        }
    }

    /**
     * Загружает список арендодателей из сервиса и обновляет интерфейс.
     */
    private void loadLandlords() {
        try {
            landlordObservableList.setAll(landlordService.getAll());
            landlordListView.refresh();
            if (landlordListView.getSelectionModel().getSelectedItem() == null) {
                clearLandlordDetails();
            }
        } catch (StorageException e) {
            handleException(e);
        }
    }

    /**
     * Применяет поисковый запрос к списку арендодателей.
     *
     * @param query поисковый запрос для фильтрации арендодателей
     */
    private void applyLandlordSearch(String query) {
        try {
            landlordObservableList.setAll(landlordService.search(query));
            landlordListView.refresh();
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Отображает детальную информацию о выбранном арендодателе.
     *
     * @param landlord арендодатель для отображения, если null - очищает детали
     */
    private void showLandlordDetails(Landlord landlord) {
        if (landlord == null) {
            clearLandlordDetails();
            return;
        }
        detailsLandlordPlaceholder.setVisible(false);
        detailsLandlordBox.setVisible(true);
        lblLandlordName.setText(landlord.getFullName());
        lblLandlordPhone.setText("Телефон: " + safe(landlord.getPhone()));
        lblLandlordEmail.setText("Email: " + safe(landlord.getEmail()));
        lblLandlordNotes.setText("Заметки: " + safe(landlord.getNotes()));
    }

    /**
     * Очищает панель с детальной информацией об арендодателе.
     */
    private void clearLandlordDetails() {
        detailsLandlordPlaceholder.setVisible(true);
        detailsLandlordBox.setVisible(false);
        lblLandlordName.setText("");
        lblLandlordPhone.setText("");
        lblLandlordEmail.setText("");
        lblLandlordNotes.setText("");
    }

    /**
     * Обработчик события добавления нового арендодателя.
     * Открывает диалоговое окно для ввода данных арендодателя.
     */
    @FXML
    private void onAddLandlord() {
        Dialog<Landlord> dialog = createLandlordDialog("Добавить арендодателя", null);
        dialog.showAndWait().ifPresent(l -> {
            try {
                landlordService.addLandlord(
                        l.getFullName(),
                        l.getPhone(),
                        l.getEmail(),
                        l.getNotes()
                );
                loadLandlords();
                logger.info("Арендодатель добавлен: {}", l.getFullName());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        });
    }

    /**
     * Обработчик события редактирования выбранного арендодателя.
     * Открывает диалоговое окно с текущими данными арендодателя.
     */
    @FXML
    private void onEditLandlord() {
        Landlord selected = landlordListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите арендодателя");
            return;
        }

        Dialog<Landlord> dialog = createLandlordDialog("Редактировать арендодателя", selected);
        dialog.showAndWait().ifPresent(updated -> {
            try {
                landlordService.updateLandlord(
                        selected,
                        updated.getFullName(),
                        updated.getPhone(),
                        updated.getEmail(),
                        updated.getNotes()
                );
                landlordListView.refresh();
                showLandlordDetails(selected);
                logger.info("Арендодатель обновлён: {}", selected.getFullName());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        });
    }

    /**
     * Обработчик события удаления выбранного арендодателя.
     * Запрашивает подтверждение перед удалением.
     */
    @FXML
    private void onDeleteLandlord() {
        Landlord selected = landlordListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите арендодателя");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить арендодателя?\n" + selected.getFullName(),
                ButtonType.OK, ButtonType.CANCEL);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                landlordService.deleteLandlord(selected);
                loadLandlords();
                logger.info("Арендодатель удалён: {}", selected.getFullName());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        }
    }

    /**
     * Загружает список объектов недвижимости из сервиса и обновляет интерфейс.
     * Также обновляет список объектов в калькуляторе платежей.
     */
    private void loadProperties() {
        try {
            propertyObservableList.setAll(propertyService.getAll());
            propertyListView.refresh();
            if (propertyListView.getSelectionModel().getSelectedItem() == null) {
                clearPropertyDetails();
            }
            // обновление списка в калькуляторе
            if (calcPropertyBox != null) {
                calcPropertyBox.setItems(FXCollections.observableArrayList(propertyService.getAll()));
            }
        } catch (StorageException e) {
            handleException(e);
        }
    }

    /**
     * Применяет поисковый запрос к списку объектов недвижимости.
     *
     * @param query поисковый запрос для фильтрации объектов
     */
    private void applyPropertySearch(String query) {
        try {
            propertyObservableList.setAll(propertyService.search(query));
            propertyListView.refresh();
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Отображает детальную информацию о выбранном объекте недвижимости.
     *
     * @param p объект недвижимости для отображения, если null - очищает детали
     */
    private void showPropertyDetails(Property p) {
        if (p == null) {
            clearPropertyDetails();
            return;
        }
        detailsPropertyPlaceholder.setVisible(false);
        detailsPropertyBox.setVisible(true);
        lblPropertyTitle.setText(p.getTitle());
        lblPropertyAddress.setText("Адрес: " + safe(p.getAddress()));
        lblPropertyArea.setText("Площадь: " + p.getArea() + " м²");
        lblPropertyPrice.setText("Аренда: " + p.getPrice() + " ₸ / месяц");
        // длинные описания не ломают интерфейс
        lblPropertyNotes.setWrapText(true);
        lblPropertyNotes.setMaxWidth(600);
        lblPropertyNotes.setText("Описание: " + safe(p.getNotes()));
    }

    /**
     * Очищает панель с детальной информацией об объекте недвижимости.
     */
    private void clearPropertyDetails() {
        detailsPropertyPlaceholder.setVisible(true);
        detailsPropertyBox.setVisible(false);
        lblPropertyTitle.setText("");
        lblPropertyAddress.setText("");
        lblPropertyArea.setText("");
        lblPropertyPrice.setText("");
        lblPropertyNotes.setText("");
    }

    /**
     * Обработчик события добавления нового объекта недвижимости.
     * Открывает диалоговое окно для ввода данных объекта.
     */
    @FXML
    private void onAddProperty() {
        Dialog<Property> dialog = createPropertyDialog("Добавить объект", null);
        dialog.showAndWait().ifPresent(p -> {
            try {
                propertyService.add(
                        p.getTitle(),
                        p.getAddress(),
                        p.getArea(),
                        p.getPrice(),
                        p.getNotes()
                );
                loadProperties();
                logger.info("Объект создан: {}", p.getTitle());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        });
    }

    /**
     * Обработчик события редактирования выбранного объекта недвижимости.
     * Открывает диалоговое окно с текущими данными объекта.
     */
    @FXML
    private void onEditProperty() {
        Property selected = propertyListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите объект");
            return;
        }

        Dialog<Property> dialog = createPropertyDialog("Редактировать объект", selected);
        dialog.showAndWait().ifPresent(updated -> {
            try {
                propertyService.update(
                        selected,
                        updated.getTitle(),
                        updated.getAddress(),
                        updated.getArea(),
                        updated.getPrice(),
                        updated.getNotes()
                );
                propertyListView.refresh();
                showPropertyDetails(selected);
                logger.info("Объект обновлён: {}", selected.getTitle());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        });
    }

    /**
     * Обработчик события удаления выбранного объекта недвижимости.
     * Запрашивает подтверждение перед удалением.
     */
    @FXML
    private void onDeleteProperty() {
        Property selected = propertyListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите объект");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Удалить объект?\n" + selected.getTitle(),
                ButtonType.OK, ButtonType.CANCEL
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                propertyService.delete(selected);
                loadProperties();
                logger.info("Объект удалён: {}", selected.getTitle());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        }
    }

    /**
     * Загружает список договоров из сервиса и обновляет интерфейс.
     * Применяет текущие фильтры поиска.
     */
    private void loadContracts() {
        try {
            applyContractsFilter();
            contractListView.refresh();
            if (contractListView.getSelectionModel().getSelectedItem() == null) {
                clearContractDetails();
            }
        } catch (StorageException e) {
            handleException(e);
        }
    }

    /**
     * Применяет комбинированный фильтр (поиск по тексту и фильтр по статусу)
     * к списку договоров.
     */
    private void applyContractsFilter() {
        try {
            String query = searchFieldContracts.getText();
            String statusFilter = contractsFilterCombo.getValue();
            var all = contractService.getAll();
            String q = (query == null) ? "" : query.toLowerCase();

            var filtered = all.stream()
                    .filter(c -> {
                        boolean matchQuery = q.isBlank()
                                || (c.getTenant() != null && c.getTenant().getFullName() != null
                                && c.getTenant().getFullName().toLowerCase().contains(q))
                                || (c.getLandlord() != null && c.getLandlord().getFullName() != null
                                && c.getLandlord().getFullName().toLowerCase().contains(q))
                                || (c.getProperty() != null && c.getProperty().getTitle() != null
                                && c.getProperty().getTitle().toLowerCase().contains(q));

                        boolean matchStatus = statusFilter == null
                                || statusFilter.equals("Все")
                                || (c.getStatus() != null && statusFilter.equalsIgnoreCase(c.getStatus()));

                        return matchQuery && matchStatus;
                    })
                    .collect(Collectors.toList());

            contractObservableList.setAll(filtered);
        } catch (StorageException e) {
            handleException(e);
        }
    }

    /**
     * Отображает детальную информацию о выбранном договоре.
     *
     * @param c договор для отображения, если null - очищает детали
     */
    private void showContractDetails(Contract c) {
        if (c == null) {
            clearContractDetails();
            return;
        }
        detailsContractPlaceholder.setVisible(false);
        detailsContractBox.setVisible(true);
        lblContractTenant.setText("Арендатор: " + (c.getTenant() != null ? safe(c.getTenant().getFullName()) : "-"));
        lblContractLandlord.setText("Арендодатель: " + (c.getLandlord() != null ? safe(c.getLandlord().getFullName()) : "-"));
        lblContractProperty.setText("Объект: " + (c.getProperty() != null ? safe(c.getProperty().getTitle()) : "-"));

        String period = "-";
        if (c.getStartDate() != null && c.getEndDate() != null) {
            period = c.getStartDate().format(dateFormatter)
                    + " — "
                    + c.getEndDate().format(dateFormatter);
        }
        lblContractPeriod.setText("Период: " + period);
        lblContractRent.setText("Аренда в месяц: " + c.getMonthlyRent());
        lblContractStatus.setWrapText(true);

        // Получаем платежи по договору
        var payments = paymentService.getByContract(c);

        // Сортировка по дате (от ранних к поздним)
        payments.sort((p1, p2) -> p1.getDate().compareTo(p2.getDate()));

        // Формируем текст истории платежей
        StringBuilder history = new StringBuilder();

        if (payments.isEmpty()) {
            history.append("Нет платежей");
        } else {
            for (Payment p : payments) {
                history.append(p.getDate().format(dateFormatter))
                        .append(" — ")
                        .append(p.getAmount()).append(" ₸ ")
                        .append("(").append(p.getType()).append(")")
                        .append("\n");
            }
        }

        // Удаляем последний символ переноса строки, если есть
        String historyText = history.toString();
        if (!historyText.isEmpty() && !historyText.equals("Нет платежей")) {
            historyText = historyText.trim();
        }

        double paidTotal = payments.stream().mapToDouble(Payment::getAmount).sum();
        double balance = paymentService.calculateBalance(c);

        lblContractStatus.setText("Статус: " + safe(c.getStatus()) +
                "\nОплачено: " + paidTotal + " ₸" +
                "\nБаланс: " + balance + " ₸" +
                "\n\nИстория платежей:\n" + historyText);
    }

    /**
     * Очищает панель с детальной информацией о договоре.
     */
    private void clearContractDetails() {
        detailsContractPlaceholder.setVisible(true);
        detailsContractBox.setVisible(false);
        lblContractTenant.setText("");
        lblContractLandlord.setText("");
        lblContractProperty.setText("");
        lblContractPeriod.setText("");
        lblContractRent.setText("");
        lblContractStatus.setText("");
    }

    /**
     * Обработчик события создания нового договора.
     * Проверяет наличие необходимых данных перед открытием диалога.
     */
    @FXML
    private void onAddContract() {
        // простая пользовательская проверка до диалога
        try {
            if (tenantService.getAll().isEmpty()
                    || landlordService.getAll().isEmpty()
                    || propertyService.getAll().isEmpty()) {
                throw new ValidationException(
                        "Для создания договора необходимы:\n"
                                + "• хотя бы один арендатор\n"
                                + "• хотя бы один арендодатель\n"
                                + "• хотя бы один объект"
                );
            }
        } catch (ValidationException e) {
            handleException(e);
            return;
        } catch (StorageException e) {
            handleException(e);
            return;
        }

        Dialog<Contract> dialog = createContractDialog("Создать договор", null);
        dialog.showAndWait().ifPresent(c -> {
            try {
                contractService.add(c);
                loadContracts();
                logger.info("Договор создан: {}", c.getId());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        });
    }

    /**
     * Обработчик события редактирования выбранного договора.
     * Открывает диалоговое окно с текущими данными договора.
     */
    @FXML
    private void onEditContract() {
        Contract selected = contractListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите договор");
            return;
        }

        Dialog<Contract> dialog = createContractDialog("Редактировать договор", selected);
        dialog.showAndWait().ifPresent(updated -> {
            try {
                contractService.update(selected, updated);
                contractListView.refresh();
                showContractDetails(selected);
                logger.info("Договор обновлён: {}", selected.getId());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        });
    }

    /**
     * Обработчик события удаления выбранного договора.
     * Запрашивает подтверждение перед удалением.
     */
    @FXML
    private void onDeleteContract() {
        Contract selected = contractListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите договор");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Удалить договор?\n" + selected,
                ButtonType.OK, ButtonType.CANCEL
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                contractService.delete(selected);
                loadContracts();
                logger.info("Договор удалён: {}", selected.getId());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        }
    }

    /**
     * Загружает список платежей из сервиса и обновляет интерфейс.
     * Обновляет счетчик платежей.
     */
    private void loadPayments() {
        try {
            paymentObservableList.setAll(paymentService.getAll());
            paymentListView.refresh();
            if (lblPaymentsCount != null) {
                lblPaymentsCount.setText(String.valueOf(paymentObservableList.size()));
            }
            if (paymentListView.getSelectionModel().getSelectedItem() == null) {
                clearPaymentDetails();
            }
        } catch (StorageException e) {
            handleException(e);
        }
    }

    /**
     * Применяет поисковый запрос к списку платежей.
     *
     * @param query поисковый запрос для фильтрации платежей
     */
    private void applyPaymentSearch(String query) {
        try {
            paymentObservableList.setAll(paymentService.search(query));
            paymentListView.refresh();
        } catch (StorageException e) {
            handleException(e);
        }
    }

    /**
     * Отображает детальную информацию о выбранном платеже.
     *
     * @param p платеж для отображения, если null - очищает детали
     */
    private void showPaymentDetails(Payment p) {
        if (p == null) {
            clearPaymentDetails();
            return;
        }
        detailsPaymentPlaceholder.setVisible(false);
        detailsPaymentBox.setVisible(true);
        lblPaymentDate.setText("Дата: " + (p.getDate() != null ? p.getDate().format(dateFormatter) : "-"));
        lblPaymentAmount.setText("Сумма: " + p.getAmount() + " ₸");
        lblPaymentType.setText("Тип: " + safe(p.getType()));

        String contractName = "-";
        if (p.getContract() != null) {
            String tenantName = p.getContract().getTenant() != null
                    ? safe(p.getContract().getTenant().getFullName()) : "?";
            String propTitle = p.getContract().getProperty() != null
                    ? safe(p.getContract().getProperty().getTitle()) : "?";
            contractName = tenantName + " • " + propTitle;
        }
        lblPaymentContract.setText("Договор: " + contractName);
        lblPaymentNotes.setWrapText(true);
        lblPaymentNotes.setMaxWidth(600);
        lblPaymentNotes.setText("Заметки: " + safe(p.getNotes()));
    }

    /**
     * Очищает панель с детальной информацией о платеже.
     */
    private void clearPaymentDetails() {
        detailsPaymentPlaceholder.setVisible(true);
        detailsPaymentBox.setVisible(false);
        lblPaymentDate.setText("");
        lblPaymentAmount.setText("");
        lblPaymentType.setText("");
        lblPaymentContract.setText("");
        lblPaymentNotes.setText("");
    }

    /**
     * Обработчик события добавления нового платежа.
     * Открывает диалоговое окно для ввода данных платежа.
     */
    @FXML
    private void onAddPayment() {
        Dialog<Payment> dialog = createPaymentDialog("Добавить платёж", null);
        dialog.showAndWait().ifPresent(p -> {
            try {
                paymentService.add(
                        p.getContract(),
                        p.getDate(),
                        p.getAmount(),
                        p.getType(),
                        p.getNotes()
                );
                loadPayments();
                loadContracts(); // статус договора может поменяться
                // Обновить отображение выбранного договора сразу, без перезапуска
                showContractDetails(contractListView.getSelectionModel().getSelectedItem());

                logger.info("Платёж создан");
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        });
    }

    /**
     * Обработчик события редактирования выбранного платежа.
     * Открывает диалоговое окно с текущими данными платежа.
     */
    @FXML
    private void onEditPayment() {
        Payment selected = paymentListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите платёж");
            return;
        }

        Dialog<Payment> dialog = createPaymentDialog("Редактировать платёж", selected);
        dialog.showAndWait().ifPresent(p -> {
            try {
                paymentService.update(
                        selected,
                        p.getContract(),
                        p.getDate(),
                        p.getAmount(),
                        p.getType(),
                        p.getNotes()
                );
                paymentListView.refresh();
                showPaymentDetails(selected);
                loadContracts();
                showContractDetails(contractListView.getSelectionModel().getSelectedItem());
                logger.info("Платёж обновлён: {}", selected.getId());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        });
    }

    /**
     * Обработчик события удаления выбранного платежа.
     * Запрашивает подтверждение перед удалением.
     */
    @FXML
    private void onDeletePayment() {
        Payment selected = paymentListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите платёж");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Удалить платёж?",
                ButtonType.OK, ButtonType.CANCEL
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                paymentService.delete(selected);
                loadPayments();
                loadContracts();
                showContractDetails(contractListView.getSelectionModel().getSelectedItem());
                logger.info("Платёж удалён: {}", selected.getId());
            } catch (ValidationException | StorageException e) {
                handleException(e);
            }
        }
    }

    /**
     * Создает диалоговое окно для ввода данных платежа.
     *
     * @param title заголовок диалогового окна
     * @param existing существующий платеж для редактирования, или null для создания нового
     * @return настроенное диалоговое окно
     */
    private Dialog<Payment> createPaymentDialog(String title, Payment existing) {
        Dialog<Payment> dialog = new Dialog<>();
        dialog.setTitle(title);
        ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker();
        TextField amountField = new TextField();
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.setItems(FXCollections.observableArrayList(
                "Аренда", "Коммуналка", "Штраф", "Депозит"
        ));

        ComboBox<Contract> contractBox = new ComboBox<>();
        try {
            contractBox.setItems(FXCollections.observableArrayList(contractService.getAll()));
        } catch (StorageException e) {
            handleException(e);
        }

        TextArea notesArea = new TextArea();
        notesArea.setWrapText(true);

        if (existing != null) {
            datePicker.setValue(existing.getDate());
            amountField.setText(String.valueOf(existing.getAmount()));
            typeBox.setValue(existing.getType());
            contractBox.setValue(existing.getContract());
            notesArea.setText(existing.getNotes());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(new Label("Дата:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Сумма:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Тип:"), 0, 2);
        grid.add(typeBox, 1, 2);
        grid.add(new Label("Договор:"), 0, 3);
        grid.add(contractBox, 1, 3);
        grid.add(new Label("Заметки:"), 0, 4);
        grid.add(notesArea, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                try {
                    if (datePicker.getValue() == null) {
                        throw new ValidationException("Укажите дату платежа");
                    }

                    double amount;
                    try {
                        amount = Double.parseDouble(amountField.getText().trim());
                    } catch (Exception e) {
                        throw new ValidationException("Сумма должна быть числом");
                    }

                    if (amount <= 0) {
                        throw new ValidationException("Сумма должна быть больше нуля");
                    }

                    if (typeBox.getValue() == null) {
                        throw new ValidationException("Выберите тип платежа");
                    }

                    return new Payment(
                            contractBox.getValue(),
                            datePicker.getValue(),
                            amount,
                            typeBox.getValue(),
                            notesArea.getText()
                    );
                } catch (ValidationException e) {
                    handleException(e);
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    /**
     * Обработчик события принудительного обновления всех данных.
     * Перезагружает данные всех сущностей системы.
     */
    @FXML
    private void onRefresh() {
        try {
            loadTenants();
            loadLandlords();
            loadProperties();
            loadContracts();
            loadPayments();
            logger.info("Данные обновлены пользователем вручную.");
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Обработчик события расчета графика платежей.
     * Выполняет расчет арендных платежей с учетом частичных месяцев.
     */
    @FXML
    private void onCalculateSchedule() {
        if (calcStartPicker == null || calcEndPicker == null || calcRentField == null) {
            showWarning("Калькулятор ещё не инициализирован.");
            return;
        }

        LocalDate start = calcStartPicker.getValue();
        LocalDate end = calcEndPicker.getValue();

        if (start == null || end == null) {
            showWarning("Выберите даты начала и окончания аренды.");
            return;
        }

        if (end.isBefore(start)) {
            showWarning("Дата окончания не может быть раньше даты начала.");
            return;
        }

        double monthlyRent;
        try {
            String rentText = calcRentField.getText();
            if ((rentText == null || rentText.isBlank())
                    && calcPropertyBox != null && calcPropertyBox.getValue() != null) {
                monthlyRent = calcPropertyBox.getValue().getPrice();
            } else {
                monthlyRent = Double.parseDouble(rentText.trim());
            }
        } catch (Exception e) {
            showWarning("Аренда в месяц должна быть числом.");
            return;
        }

        if (monthlyRent <= 0) {
            showWarning("Аренда в месяц должна быть больше нуля.");
            return;
        }

        if (calcObservableList == null) {
            calcObservableList = FXCollections.observableArrayList();
            if (calcTable != null) {
                calcTable.setItems(calcObservableList);
            }
        }

        calcObservableList.clear();
        double total = 0.0;
        LocalDate monthCursor = start.withDayOfMonth(1);

        while (!monthCursor.isAfter(end)) {
            LocalDate monthStart = monthCursor;
            LocalDate monthEnd = monthCursor.withDayOfMonth(monthCursor.lengthOfMonth());

            LocalDate periodStart = monthStart.isBefore(start) ? start : monthStart;
            LocalDate periodEnd = monthEnd.isAfter(end) ? end : monthEnd;

            long days = ChronoUnit.DAYS.between(periodStart, periodEnd.plusDays(1));

            if (days > 0) {
                int daysInMonth = monthStart.lengthOfMonth();
                double daily = monthlyRent / daysInMonth;
                double amount = daily * days;
                total += amount;

                String monthLabel = String.format("%02d.%d", monthStart.getMonthValue(), monthStart.getYear());
                String periodLabel = periodStart.format(dateFormatter) + " — " + periodEnd.format(dateFormatter);

                calcObservableList.add(
                        new ScheduleRow(
                                monthLabel,
                                periodLabel,
                                String.valueOf(days),
                                String.format("%.2f", amount)
                        )
                );
            }
            monthCursor = monthCursor.plusMonths(1);
        }

        long totalDays = ChronoUnit.DAYS.between(start, end.plusDays(1));
        int monthsCount = calcObservableList.size();

        if (lblCalcPeriod != null) {
            lblCalcPeriod.setText("Период: " + start.format(dateFormatter) + " — "
                    + end.format(dateFormatter) + " (" + totalDays + " дней)");
        }

        if (lblCalcMonths != null) {
            lblCalcMonths.setText("Месяцев (по календарю): " + monthsCount);
        }

        if (lblCalcTotal != null) {
            lblCalcTotal.setText("Итого: " + String.format("%.2f", total));
        }
    }

    /**
     * Класс, представляющий строку в таблице графика платежей.
     * Используется для отображения расчетов в калькуляторе.
     */
    public static class ScheduleRow {
        private final SimpleStringProperty month;
        private final SimpleStringProperty period;
        private final SimpleStringProperty days;
        private final SimpleStringProperty amount;

        /**
         * Создает новую строку графика платежей.
         *
         * @param month название месяца (формат "MM.ГГГГ")
         * @param period период аренды в пределах месяца
         * @param days количество дней аренды в месяце
         * @param amount сумма аренды за период
         */
        public ScheduleRow(String month, String period, String days, String amount) {
            this.month = new SimpleStringProperty(month);
            this.period = new SimpleStringProperty(period);
            this.days = new SimpleStringProperty(days);
            this.amount = new SimpleStringProperty(amount);
        }

        /**
         * Возвращает название месяца.
         *
         * @return название месяца в формате "MM.ГГГГ"
         */
        public String getMonth() {
            return month.get();
        }

        /**
         * Возвращает период аренды в пределах месяца.
         *
         * @return период в формате "дд.ММ.ГГГГ — дд.ММ.ГГГГ"
         */
        public String getPeriod() {
            return period.get();
        }

        /**
         * Возвращает количество дней аренды в месяце.
         *
         * @return количество дней
         */
        public String getDays() {
            return days.get();
        }

        /**
         * Возвращает сумму аренды за период.
         *
         * @return сумма с двумя знаками после запятой
         */
        public String getAmount() {
            return amount.get();
        }
    }

    /**
     * Создает диалоговое окно для ввода данных арендатора.
     *
     * @param title заголовок диалогового окна
     * @param existing существующий арендатор для редактирования, или null для создания нового
     * @return настроенное диалоговое окно
     */
    private Dialog<Tenant> createTenantDialog(String title, Tenant existing) {
        Dialog<Tenant> dialog = new Dialog<>();
        dialog.setTitle(title);
        ButtonType saveBtn = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField name = new TextField();
        TextField phone = new TextField();
        TextField email = new TextField();
        TextArea notes = new TextArea();
        notes.setWrapText(true);

        if (existing != null) {
            name.setText(existing.getFullName());
            phone.setText(existing.getPhone());
            email.setText(existing.getEmail());
            notes.setText(existing.getNotes());
        }

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);
        grid.add(new Label("ФИО:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Телефон:"), 0, 1);
        grid.add(phone, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(email, 1, 2);
        grid.add(new Label("Заметки:"), 0, 3);
        grid.add(notes, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    validateName(name.getText(), "ФИО");
                    validatePhone(phone.getText());
                    validateEmail(email.getText());

                    return new Tenant(
                            name.getText(),
                            phone.getText(),
                            email.getText(),
                            notes.getText()
                    );
                } catch (ValidationException e) {
                    handleException(e);
                }
            }
            return null;
        });

        return dialog;
    }

    /**
     * Создает диалоговое окно для ввода данных арендодателя.
     *
     * @param title заголовок диалогового окна
     * @param existing существующий арендодатель для редактирования, или null для создания нового
     * @return настроенное диалоговое окно
     */
    private Dialog<Landlord> createLandlordDialog(String title, Landlord existing) {
        Dialog<Landlord> dialog = new Dialog<>();
        dialog.setTitle(title);
        ButtonType saveBtn = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField name = new TextField();
        TextField phone = new TextField();
        TextField email = new TextField();
        TextArea notes = new TextArea();
        notes.setWrapText(true);

        if (existing != null) {
            name.setText(existing.getFullName());
            phone.setText(existing.getPhone());
            email.setText(existing.getEmail());
            notes.setText(existing.getNotes());
        }

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);
        grid.add(new Label("ФИО:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Телефон:"), 0, 1);
        grid.add(phone, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(email, 1, 2);
        grid.add(new Label("Заметки:"), 0, 3);
        grid.add(notes, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    validateName(name.getText(), "ФИО");
                    validatePhone(phone.getText());
                    validateEmail(email.getText());

                    return new Landlord(
                            name.getText(),
                            phone.getText(),
                            email.getText(),
                            notes.getText()
                    );
                } catch (ValidationException e) {
                    handleException(e);
                }
            }
            return null;
        });

        return dialog;
    }

    /**
     * Создает диалоговое окно для ввода данных объекта недвижимости.
     *
     * @param title заголовок диалогового окна
     * @param existing существующий объект для редактирования, или null для создания нового
     * @return настроенное диалоговое окно
     */
    private Dialog<Property> createPropertyDialog(String title, Property existing) {
        Dialog<Property> dialog = new Dialog<>();
        dialog.setTitle(title);
        ButtonType saveBtn = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField titleField = new TextField();
        TextField addressField = new TextField();
        TextField areaField = new TextField();
        TextField priceField = new TextField();
        TextArea notesArea = new TextArea();
        notesArea.setWrapText(true);

        if (existing != null) {
            titleField.setText(existing.getTitle());
            addressField.setText(existing.getAddress());
            areaField.setText(String.valueOf(existing.getArea()));
            priceField.setText(String.valueOf(existing.getPrice()));
            notesArea.setText(existing.getNotes());
        }

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);
        grid.add(new Label("Название:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Адрес:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("Площадь (м²):"), 0, 2);
        grid.add(areaField, 1, 2);
        grid.add(new Label("Аренда / месяц:"), 0, 3);
        grid.add(priceField, 1, 3);
        grid.add(new Label("Описание:"), 0, 4);
        grid.add(notesArea, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    String titleText = validateText(titleField.getText(), "Название объекта");
                    String address = validateAddress(addressField.getText());
                    double area = validateDouble(areaField.getText(), "Площадь");
                    double price = validateDouble(priceField.getText(), "Аренда");

                    return new Property(
                            titleText,
                            address,
                            area,
                            price,
                            notesArea.getText()
                    );
                } catch (ValidationException e) {
                    handleException(e);
                }
            }
            return null;
        });

        return dialog;
    }

    /**
     * Создает диалоговое окно для ввода данных договора аренды.
     *
     * @param title заголовок диалогового окна
     * @param existing существующий договор для редактирования, или null для создания нового
     * @return настроенное диалоговое окно
     */
    private Dialog<Contract> createContractDialog(String title, Contract existing) {
        Dialog<Contract> dialog = new Dialog<>();
        dialog.setTitle(title);
        ButtonType saveBtn = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        ComboBox<Tenant> tenantBox = new ComboBox<>();
        ComboBox<Landlord> landlordBox = new ComboBox<>();
        ComboBox<Property> propertyBox = new ComboBox<>();
        DatePicker startPicker = new DatePicker();
        DatePicker endPicker = new DatePicker();
        TextField rentField = new TextField();

        try {
            tenantBox.setItems(FXCollections.observableArrayList(tenantService.getAll()));
            landlordBox.setItems(FXCollections.observableArrayList(landlordService.getAll()));
            propertyBox.setItems(FXCollections.observableArrayList(propertyService.getAll()));
        } catch (StorageException e) {
            handleException(e);
        }

        if (existing != null) {
            tenantBox.setValue(existing.getTenant());
            landlordBox.setValue(existing.getLandlord());
            propertyBox.setValue(existing.getProperty());
            startPicker.setValue(existing.getStartDate());
            endPicker.setValue(existing.getEndDate());
            rentField.setText(String.valueOf(existing.getMonthlyRent()));
        }

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);
        grid.add(new Label("Арендатор:"), 0, 0);
        grid.add(tenantBox, 1, 0);
        grid.add(new Label("Арендодатель:"), 0, 1);
        grid.add(landlordBox, 1, 1);
        grid.add(new Label("Объект:"), 0, 2);
        grid.add(propertyBox, 1, 2);
        grid.add(new Label("Дата начала:"), 0, 3);
        grid.add(startPicker, 1, 3);
        grid.add(new Label("Дата окончания:"), 0, 4);
        grid.add(endPicker, 1, 4);
        grid.add(new Label("Аренда / месяц:"), 0, 5);
        grid.add(rentField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    if (tenantBox.getValue() == null || landlordBox.getValue() == null
                            || propertyBox.getValue() == null)
                        throw new ValidationException("Выберите арендатора, арендодателя и объект");

                    if (startPicker.getValue() == null || endPicker.getValue() == null)
                        throw new ValidationException("Укажите даты");

                    double rent = Double.parseDouble(rentField.getText());

                    return new Contract(
                            tenantBox.getValue(),
                            landlordBox.getValue(),
                            propertyBox.getValue(),
                            startPicker.getValue(),
                            endPicker.getValue(),
                            rent
                    );
                } catch (Exception e) {
                    handleException(new ValidationException("Ошибка заполнения полей договора."));
                }
            }
            return null;
        });

        return dialog;
    }

    /**
     * Показывает предупреждающее сообщение пользователю.
     *
     * @param content текст предупреждения
     */
    private void showWarning(String content) {
        logger.warn("Пользовательское предупреждение: {}", content);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Показывает сообщение об ошибке пользователю.
     *
     * @param content текст ошибки
     */
    private void showError(String content) {
        logger.error("Ошибка приложения: {}", content);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Ошибка");
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Обрабатывает исключения, возникшие в приложении.
     * В зависимости от типа исключения показывает соответствующее сообщение
     * и записывает информацию в лог.
     *
     * @param e исключение для обработки
     */
    private void handleException(Exception e) {
        if (e instanceof ValidationException ve) {
            logger.warn("Ошибка валидации: {}", ve.getMessage());
            showWarning(ve.getMessage());
        } else if (e instanceof StorageException se) {
            logger.error("Ошибка хранения данных", se);
            showError("Ошибка при работе с файлами данных.\nПодробности см. в логе.");
        } else {
            logger.error("Необработанная ошибка", e);
            showError("Неизвестная ошибка. Подробности см. в логе.");
        }
    }
}