package generators.testdata;

import generators.RandomModelGenerator;

public enum InvalidTransferCase {
    NEGATIVE("^-[1-9]\\d*\\.\\d{2}$"),
    ZERO("^0\\.0{2}$"),
    ABOVE_MAX("^1[0-9]{4}\\.\\d{2}$"),
    INSUFFICIENT_FUNDS("^[1-9]\\d{3,4}\\.\\d{2}$");  // суммы > баланса

    private final String regex;

    InvalidTransferCase(String regex) {
        this.regex = regex;
    }

    public double generate() {
        if (this == ZERO) return 0.0;
        return Double.parseDouble(RandomModelGenerator.generateFromRegex(regex));
    }
}