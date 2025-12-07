package rentcontrol;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import rentcontrol.exceptions.StorageException;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Универсальное хранилище JSON-файлов.
 * <p>
 * Класс обеспечивает сериализацию и десериализацию объектов выбранного типа
 * с использованием GSON, поддерживает LocalDate и централизованно генерирует
 * исключения StorageException при ошибках чтения/записи.
 * </p>
 *
 * @param <T> тип объектов, хранимых в файле
 */
public class JsonStorage<T> {

    /** Путь к JSON-файлу. */
    private final Path filePath;

    /** Конфигурация GSON с поддержкой LocalDate. */
    private final Gson gson;

    /** Класс типа данных, которые сохраняются. */
    private final Class<T> clazz;

    /**
     * Создаёт JSON-хранилище для указанного файла и типа данных.
     *
     * @param filePath путь к JSON-файлу
     * @param clazz класс хранимой сущности
     */
    public JsonStorage(String filePath, Class<T> clazz) {
        this.filePath = Path.of(filePath);
        this.clazz = clazz;

        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, type, ctx) ->
                                LocalDate.parse(json.getAsString()))
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (date, type, ctx) ->
                                new JsonPrimitive(date.toString()))
                .setPrettyPrinting()
                .create();
    }

    /**
     * Загружает список объектов из JSON.
     *
     * @return список сущностей или пустой список, если файл отсутствует
     * @throws StorageException если произошла ошибка чтения файла
     */
    public List<T> loadAll() {
        try {
            if (!Files.exists(filePath)) {
                return new ArrayList<>();
            }

            Type listType = TypeToken.getParameterized(List.class, clazz).getType();

            try (FileReader reader = new FileReader(filePath.toFile())) {
                List<T> data = gson.fromJson(reader, listType);
                return data != null ? data : new ArrayList<>();
            }

        } catch (Exception e) {
            throw new StorageException("Ошибка чтения файла: " + filePath, e);
        }
    }

    /**
     * Сохраняет список объектов в JSON-файл.
     *
     * @param data список сущностей
     * @throws StorageException при ошибке записи файла
     */
    public void saveAll(List<T> data) {
        try {
            Files.createDirectories(filePath.getParent());

            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                gson.toJson(data, writer);
            }

        } catch (Exception e) {
            throw new StorageException("Ошибка сохранения файла: " + filePath, e);
        }
    }
}
