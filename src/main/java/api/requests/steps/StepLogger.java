package api.requests.steps;

import io.qameta.allure.Allure;
import java.util.function.Supplier;

public class StepLogger {

    public static <T> T log(String stepName, Supplier<T> step) {
        return Allure.step(stepName, () -> step.get());
    }

    public static void log(String stepName, Runnable step) {
        Allure.step(stepName, step::run);
    }
}