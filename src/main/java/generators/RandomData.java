package generators;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomData {
    private RandomData() {}

    public static String getUsername() {
        return RandomStringUtils.randomAlphabetic(3)
                + RandomStringUtils.randomAlphanumeric(3);
    }

    public static String getPassword() {
        return RandomStringUtils.randomAlphabetic(5).toUpperCase() +
                RandomStringUtils.randomAlphabetic(4).toLowerCase() +
                RandomStringUtils.randomNumeric(3) + "&";
    }
}
