package common.extensions;

import api.configs.Config;
import common.annotations.Environments;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;

public class EnvironmentMatchExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        // ШАГ 1: Проверка есть ли у теста аннотация Environments
        Environments annotation = extensionContext.getTestMethod()
                .map(method -> method.getAnnotation(Environments.class))
                .orElse(null);

        // Если аннотации нет - тест выполняется
        if (annotation == null) {
            return ConditionEvaluationResult.enabled("Нет ограничений к окружению");
        }


        String currentEnvironment = Config.getTestEnvironment();
        String[] allowedEnvironments = annotation.value();

        // Если список разрешённых окружений пуст - тест выполняется
        if (allowedEnvironments.length == 0) {
            return ConditionEvaluationResult.enabled("В аннотации не указаны допустимые окружения");
        }


        boolean matches = Arrays.stream(allowedEnvironments)
                .anyMatch(env -> env.equalsIgnoreCase(currentEnvironment));

        if (matches) {
            return ConditionEvaluationResult.enabled(
                    String.format("Окружение '%s' разрешено для этого теста", currentEnvironment)
            );
        } else {
            return ConditionEvaluationResult.disabled(
                    String.format("Тест пропущен. Текущее окружение '%s' отсутствует в списке: %s",
                            currentEnvironment, Arrays.toString(allowedEnvironments))
            );
        }
    }
}