package utils;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import common.helpers.StepLogger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Универсальный помощник для ожиданий с повторными попытками
 */
public class WaitUtils {

    private static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private static final int DEFAULT_RETRY_COUNT = 3;
    private static final int RETRY_DELAY_MS = 500;
    private static final String DEFAULT_TITLE = "Retry action";

    // ========== Базовые методы с повторными попытками ==========

    // Каждая попытка - вложенный шаг Allure внутри шага title
    public static <T> T doWithRetry(String title, Supplier<T> action, Predicate<T> condition,
                                    int maxRetries, int delayMs) {
        return StepLogger.log(title, () -> {
            Throwable lastError = null;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    return StepLogger.log("Attempt " + attempt + "/" + maxRetries, () -> {
                        T result = action.get();
                        if (!condition.test(result)) {
                            throw new AssertionError("Condition not met, result: " + result);
                        }
                        return result;
                    });
                } catch (AssertionError | TimeoutException e) {
                    lastError = e;
                    if (attempt < maxRetries) {
                        System.out.println("Attempt " + attempt + "/" + maxRetries + " failed, retrying...");
                        sleep(delayMs);
                    }
                }
            }
            throw new AssertionError(String.format("%s: failed after %d attempts. Last error: %s",
                    title, maxRetries, lastError.getMessage()), lastError);
        });
    }

    public static <T> T doWithRetry(String title, Supplier<T> action, int maxRetries, int delayMs) {
        return doWithRetry(title, action, result -> true, maxRetries, delayMs);
    }

    public static <T> T doWithRetry(String title, Supplier<T> action) {
        return doWithRetry(title, action, DEFAULT_RETRY_COUNT, RETRY_DELAY_MS);
    }

    public static void doWithRetry(String title, Runnable action, int maxRetries, int delayMs) {
        doWithRetry(title, () -> {
            action.run();
            return null;
        }, maxRetries, delayMs);
    }

    public static void doWithRetry(String title, Runnable action) {
        doWithRetry(title, action, DEFAULT_RETRY_COUNT, RETRY_DELAY_MS);
    }

    public static <T> T doWithRetry(Supplier<T> action, int maxRetries, int delayMs) {
        return doWithRetry(DEFAULT_TITLE, action, maxRetries, delayMs);
    }

    public static <T> T doWithRetry(Supplier<T> action) {
        return doWithRetry(DEFAULT_TITLE, action);
    }

    public static void doWithRetry(Runnable action, int maxRetries, int delayMs) {
        doWithRetry(DEFAULT_TITLE, action, maxRetries, delayMs);
    }

    public static void doWithRetry(Runnable action) {
        doWithRetry(DEFAULT_TITLE, action);
    }

    // ========== Ожидания элементов ==========

    public static WebElement waitForElement(By locator, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForElement(By locator) {
        return waitForElement(locator, DEFAULT_TIMEOUT_SECONDS);
    }

    public static boolean waitForText(By locator, String expectedText, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, expectedText));
    }

    // ========== Ожидания для Selenide ==========

    public static SelenideElement waitForVisible(SelenideElement element, int timeoutSeconds) {
        return doWithRetry("Wait for element to be visible: " + element, () -> {
            element.shouldBe(com.codeborne.selenide.Condition.visible);
            return element;
        });
    }

    public static SelenideElement waitForEnabled(SelenideElement element, int timeoutSeconds) {
        return doWithRetry("Wait for element to be enabled: " + element, () -> {
            element.shouldBe(com.codeborne.selenide.Condition.enabled);
            return element;
        });
    }

    public static SelenideElement waitForClickable(SelenideElement element, int timeoutSeconds) {
        return doWithRetry("Wait for element to be clickable: " + element, () -> {
            element.shouldBe(com.codeborne.selenide.Condition.visible)
                    .shouldBe(com.codeborne.selenide.Condition.enabled);
            return element;
        });
    }

    public static void waitForText(SelenideElement element, String expectedText, int timeoutSeconds) {
        doWithRetry("Wait for element to have text '" + expectedText + "': " + element,
                () -> element.shouldHave(com.codeborne.selenide.Condition.text(expectedText)));
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}