package rentcontrol;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Главный класс JavaFX-приложения RentControl.
 * <p>
 * Отвечает за загрузку интерфейса, подключение стилей
 * и запуск основного окна.
 * </p>
 */
public class MainApp extends Application {

    /** Логгер приложения. */
    private static final Logger logger = LogManager.getLogger(MainApp.class);

    /**
     * Запускает главное окно приложения.
     *
     * @param stage корневое окно JavaFX
     */
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("main-view.fxml"));
            Scene scene = new Scene(loader.load(), 1740, 768);

            scene.getStylesheets().add(
                    Objects.requireNonNull(
                            MainApp.class.getResource("styles.css")).toExternalForm()
            );

            stage.setTitle("RentControl");
            stage.setScene(scene);
            stage.show();

            logger.info("Приложение запущено");
        } catch (Exception e) {
            logger.error("Ошибка запуска приложения", e);
        }
    }

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        launch();
    }
}
