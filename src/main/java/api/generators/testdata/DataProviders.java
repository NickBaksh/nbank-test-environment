package api.generators.testdata;

import api.generators.RandomModelGenerator;
import api.models.UpdateCustomerProfileRequest;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.stream.Stream;

import static api.specs.ResponseSpecs.*;

public class DataProviders {

    public static final Double TRANSACTION_0_0_1 = 0.01;
    public static final Double TRANSACTION_0 = 0.00;
    public static final Double TRANSACTION_1 = 1.00;
    public static final Double TRANSACTION_100 = 100.00;
    public static final Double TRANSACTION_1000 = 1000.00;
    public static final Double TRANSACTION_10000 = 10000.00;
    public static final Double TRANSACTION_10000_0_1 = 10000.01;
    public static final Double BALANCE_5000 = 5000.00;

    // ========== Валидные данные для профиля ==========

    /**
     * Генерирует одно валидное имя профиля
     */
    public static Stream<String> validProfileNames() {
        return Stream.generate(() ->
                RandomModelGenerator.generateWithBuilder(UpdateCustomerProfileRequest.class).getName()
        ).limit(1);
    }

    /**
     * Генерирует несколько валидных имён профиля (для тестов, где нужно больше данных)
     */
    public static Stream<String> validProfileNames(int count) {
        return Stream.generate(() ->
                RandomModelGenerator.generateWithBuilder(UpdateCustomerProfileRequest.class).getName()
        ).limit(count);
    }

    // ========== Невалидные данные для профиля ==========

    /**
     * Генерирует все варианты невалидных имён из InvalidNameCase
     */
    public static Stream<String> invalidProfileNames() {
        return Arrays.stream(InvalidNameCase.values())
                .map(InvalidNameCase::generate);
    }

    /**
     * Генерирует случайное невалидное имя
     */
    public static String getRandomInvalidProfileName() {
        return invalidProfileNames().findFirst().orElseThrow();
    }

    // ========== Данные для депозитов ==========

    /**
     * Генерирует валидные суммы для депозита
     */
    public static Stream<Double> validDepositAmounts() {
        return Stream.of(0.01, 1000.0, 2500.0, 4999.99, 5000.0);
    }

    /**
     * Генерирует невалидные суммы для депозита (с ожидаемыми сообщениями на UI)
     */
    public static Stream<Arguments> invalidDepositAmountsUi() {
        return Stream.of(
                Arguments.of(-0.01, "❌ Please enter a valid amount."),
                Arguments.of(0.0, "❌ Please enter a valid amount."),
                Arguments.of(5000.01, "❌ Please deposit less or equal to 5000$.")
        );
    }

    /**
     * Генерирует невалидные суммы для депозита (с ожидаемыми сообщениями на API)
     */
    public static Stream<Arguments> invalidDepositAmountsApi() {
        return Stream.of(
                Arguments.of(-0.01, DEPOSIT_AMOUNT_MIN_ERROR),
                Arguments.of(0.0, DEPOSIT_AMOUNT_MIN_ERROR),
                Arguments.of(5000.01, DEPOSIT_AMOUNT_MAX_ERROR)
        );
    }

    // ========== Данные для переводов ==========

    /**
     * Генерирует валидные суммы для перевода
     */
    public static Stream<Double> validTransferAmounts() {
        return Stream.of(0.01, 1000.0, 5000.0, 9999.99, 10000.0);
    }

    public static double getValidTransferAmount() {
        return validTransferAmounts().findAny().get();
    }

    /**
     * Генерирует невалидные суммы для перевода
     */
    public static Stream<Arguments> invalidTransferAmountsUi() {
        return Stream.of(
                Arguments.of(-0.01, "❌ Transfer amount must be at least 0.01"),
                Arguments.of(0.0, "❌ Transfer amount must be at least 0.01"),
                Arguments.of(10000.01, "❌ Transfer amount cannot exceed 10000")
        );
    }

    static Stream<Arguments> invalidTransferAmountsApi() {
        return Stream.of(
                Arguments.of(-0.01, TRANSFER_AMOUNT_MIN_ERROR),
                Arguments.of(0, TRANSFER_AMOUNT_MIN_ERROR),
                Arguments.of(10000.01, TRANSFER_AMOUNT_MAX_ERROR)
        );
    }
}