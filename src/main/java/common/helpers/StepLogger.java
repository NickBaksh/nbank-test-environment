package common.helpers;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;

import java.io.ByteArrayInputStream;

public class StepLogger {
    @FunctionalInterface
    public interface ThrowableRunnable<T> {
        T run() throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowableVoidRunnable {
        void run() throws Throwable;
    }

    public static <T> T log(String title, ThrowableRunnable<T> runnable) {
        return Allure.step(title, () -> runnable.run());
    }

    public static void log(String title, ThrowableVoidRunnable runnable) {
        Allure.step(title, () -> {
            runnable.run();
            return null;
        });
    }

    // Шаг UI: после выполнения (и при падении) к шагу прикладывается скриншот страницы
    public static <T> T logWithScreenshot(String title, ThrowableRunnable<T> runnable) {
        return Allure.step(title, () -> {
            try {
                return runnable.run();
            } finally {
                attachScreenshot();
            }
        });
    }

    public static void logWithScreenshot(String title, ThrowableVoidRunnable runnable) {
        logWithScreenshot(title, () -> {
            runnable.run();
            return null;
        });
    }

    private static void attachScreenshot() {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            return;
        }
        try {
            // Скриншот при открытом alert закрывает его, а тест дальше проверяет его текст.
            // Поэтому вместо скриншота прикладываем текст alert
            Alert alert = WebDriverRunner.getWebDriver().switchTo().alert();
            Allure.addAttachment("Alert", alert.getText());
            return;
        } catch (NoAlertPresentException ignored) {
            // alert нет - делаем скриншот
        }
        try {
            byte[] screenshot = Selenide.screenshot(OutputType.BYTES);
            if (screenshot != null) {
                Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(screenshot), "png");
            }
        } catch (WebDriverException e) {
            System.out.println("Screenshot was not taken: " + e.getMessage());
        }
    }
}
