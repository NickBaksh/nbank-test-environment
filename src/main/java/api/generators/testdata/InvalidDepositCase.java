package api.generators.testdata;

import api.generators.RandomModelGenerator;

public enum InvalidDepositCase {
    NEGATIVE("^-\\d{1,3}\\.\\d{4}$"),
    ZERO("^0\\.0{2}$"),
    ABOVE_MAX("^5[0-9]{3}\\.[0-9]{2}$");

    private final String regex;

    InvalidDepositCase(String regex) {
        this.regex = regex;
    }

    public double generate() {
        return Double.parseDouble(RandomModelGenerator.generateFromRegex(regex));
    }
}