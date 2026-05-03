package generators.testdata;

import generators.RandomModelGenerator;

public enum InvalidNameCase {
    THREE_WORDS("^[A-Za-z]{2,15} [A-Za-z]{2,15} [A-Za-z]{2,15}$"),
    ONE_WORD("^[A-Za-z]{2,15}$"),
    DIGITS_IN_FIRST("^[A-Za-z]{2,15}[0-9]{1,5} [A-Za-z]{2,15}$"),
    DIGITS_IN_SECOND("^[A-Za-z]{2,15} [A-Za-z]{2,15}[0-9]{1,5}$"),
    SPECIAL_CHAR("^[A-Za-z]{2,15}[!@#^&]{1} [A-Za-z]{2,15}$"),
    UNDERSCORE("^[A-Za-z]{2,15}_[A-Za-z]{2,15}$"),
    CYRILLIC("^[А-Яа-я]{2,15} [А-Яа-я]{2,15}$"),
    EMPTY("^$"),
    SPACE("^ $");

    private final String regex;

    InvalidNameCase(String regex) {
        this.regex = regex;
    }

    public String generate() {
        return RandomModelGenerator.generateFromRegex(regex);
    }
}