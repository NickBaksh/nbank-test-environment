package common.annotations;

import common.extensions.FraudCheckMockExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(FraudCheckMockExtension.class)
public @interface FraudCheckMock {

    String status() default "SUCCESS";

    String decision() default "APPROVED";

    double riskScore() default 0.0;

    String reason() default "Transaction processed successfully";

    boolean requiresManualReview() default false;

    boolean additionalVerificationRequired() default false;

    int httpStatus() default 200;

    // Задержка ответа в мс (для тестирования таймаутов)
    long delayMs() default 0;

    String stubName() default "";

    boolean simulateTimeout() default false;

    boolean simulateConnectionError() default false;

    int errorHttpStatus() default 0;
}
