package api.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Конфигурация тестов.
 * <p>
 * Приоритет источников (от высшего к низшему):
 * <ol>
 *     <li>System property: {@code -Dkey=value}</li>
 *     <li>Переменная окружения: {@code db.url → DB_URL}, {@code apiBaseUrl → API_BASE_URL}</li>
 *     <li>{@code config.properties} из resources</li>
 *     <li>Значение по умолчанию</li>
 * </ol>
 * Пустые и пробельные значения считаются незаданными.
 */
public final class Config {

    private static final String CONFIG_FILE = "config.properties";
    private static final Config INSTANCE = new Config();

    private final Properties properties = new Properties();

    private Config() {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException(CONFIG_FILE + " not found in resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
    }

    // ---------- Базовое чтение ----------

    /** Значение по ключу или {@code null}, если не задано ни в одном источнике. */
    public static String getProperty(String key) {
        return getProperty(key, null);
    }

    /** Значение по ключу или {@code defaultValue}, если не задано ни в одном источнике. */
    public static String getProperty(String key, String defaultValue) {
        String value = firstNonBlank(
                System.getProperty(key),
                System.getenv(toEnvKey(key)),
                INSTANCE.properties.getProperty(key)
        );
        return value != null ? value : defaultValue;
    }

    /** Значение по ключу; если не задано — понятная ошибка вместо NPE где-то в тестах. */
    public static String getRequiredProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            throw new IllegalStateException(
                    "Property '" + key + "' is not configured. Set -D" + key
                            + ", env " + toEnvKey(key) + " or add it to " + CONFIG_FILE);
        }
        return value;
    }

    // ---------- Конкретные настройки ----------

    public static String getTestEnvironment() {
        return getProperty("test.environment");
    }

    public static String getApiBaseUrl() {
        return getProperty("apiBaseUrl", "http://localhost:4111");
    }

    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:postgresql://localhost:5433/nbank");
    }

    public static String getDbUser() {
        return getProperty("db.user", "postgres");
    }

    public static String getDbPassword() {
        return getProperty("db.password", "postgres");
    }

    public static String getApiVersion() {
        return parseApiVersion(getRequiredProperty("apiVersion"));
    }

    public static String getWireMockHost() {
        return getProperty("wiremock.host", "localhost");
    }

    public static int getWireMockPort() {
        return Integer.parseInt(getProperty("wiremock.port", "8080"));
    }

    // ---------- Вспомогательное ----------

    /**
     * Приводит версию к виду {@code vN}:
     * {@code "/api/v1"} → {@code v1}, {@code "apiv2"} → {@code v2}, {@code "1"} → {@code v1}.
     */
    static String parseApiVersion(String raw) {
        String version = raw.trim();

        // берём последний непустой сегмент пути: "/api/v1/" → "v1"
        String[] parts = version.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isBlank()) {
                version = parts[i].trim();
                break;
            }
        }

        if (version.startsWith("api") && version.length() > 3) {
            version = version.substring(3);
        }

        if (version.matches("\\d.*")) {
            version = "v" + version;
        }

        if (!version.matches("v\\d+")) {
            throw new IllegalStateException("Failed to parse apiVersion: '" + raw + "' has invalid format");
        }
        return version;
    }

    /** db.url → DB_URL, apiBaseUrl → API_BASE_URL, test.environment → TEST_ENVIRONMENT */
    static String toEnvKey(String key) {
        return key
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("[.\\-]", "_")
                .toUpperCase();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    /** Проверки в БД включены, если бэкенд пишет в доступную тестам базу. */
    public static boolean isDbChecksEnabled() {
        return Boolean.parseBoolean(getProperty("db.checks.enabled", "true"));
    }

    /** Есть ли на окружении эндпоинт transfer-with-fraud-check. */
    public static boolean isFraudCheckEnabled() {
        return Boolean.parseBoolean(getProperty("feature.fraud.check.enabled", "true"));
    }
}