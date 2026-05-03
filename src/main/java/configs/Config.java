package configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private final static Config INSTANSE = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Fail to load config.properties");
        }
    }

    public static String getProperty(String key) {
        return INSTANSE.properties.getProperty(key);
    }
}
