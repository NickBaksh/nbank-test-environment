package common.extensions;

import api.configs.Config;
import common.annotations.ApiVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ApiVersionCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        ApiVersion apiVersion = context.getElement()
                .get()
                .getAnnotation(ApiVersion.class);

        if (apiVersion == null) {
            return ConditionEvaluationResult.enabled("No @ApiVersion");
        }

        try {
            String currentVersion = Config.getApiVersion();
            String requiredVersion = apiVersion.value();

            if (currentVersion.equals(requiredVersion)) {
                return ConditionEvaluationResult.enabled(
                        " API version matches: " + currentVersion
                );
            } else {
                return ConditionEvaluationResult.disabled(
                        "API version mismatch: " + currentVersion + " != " + requiredVersion
                );
            }
        } catch (Exception e) {
            // Если версия не задана или непарсится — пропускаем тест с понятным сообщением
            return ConditionEvaluationResult.disabled(
                    "❌ API version configuration error: " + e.getMessage()
            );
        }
    }
}
