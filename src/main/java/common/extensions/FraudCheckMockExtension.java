package common.extensions;

import api.models.dto_model.FraudCheckResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import common.annotations.FraudCheckMock;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class FraudCheckMockExtension implements BeforeEachCallback, AfterEachCallback {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final ThreadLocal<FraudCheckMock> currentThreadConfig = new ThreadLocal<>();
    private static final ConcurrentHashMap<Long, FraudCheckMock> threadConfigs = new ConcurrentHashMap<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        var methodAnnotation = AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                FraudCheckMock.class
        );

        if (methodAnnotation.isPresent()) {
            setupFraudCheckMockForCurrentThread(methodAnnotation.get());
            return;
        }

        var classAnnotation = AnnotationSupport.findAnnotation(
                context.getRequiredTestClass(),
                FraudCheckMock.class
        );
        classAnnotation.ifPresent(this::setupFraudCheckMockForCurrentThread);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        cleanupAfterTest();
    }

    private void setupFraudCheckMockForCurrentThread(FraudCheckMock annotation) {
        long threadId = Thread.currentThread().threadId();

        threadConfigs.put(threadId, annotation);
        currentThreadConfig.set(annotation);

        setupFraudCheckMock(annotation);

        System.out.println("FraudCheckMock configured for thread " + threadId
                + ": decision=" + annotation.decision()
                + ", status=" + annotation.status());
    }

    private void setupFraudCheckMock(FraudCheckMock annotation) {
        try {
            // ========== 1. Настройка ошибок ==========

            // 1.1 Симуляция таймаута
            if (annotation.simulateTimeout()) {
                stubFor(get(urlPathEqualTo("/events"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withFixedDelay(10000) // 10 секунд задержки (таймаут)
                        )
                );
                System.out.println("⚠️ WireMock configured with TIMEOUT simulation (10s delay)");
                return; // Не настраиваем успешный ответ
            }

            // 1.2 Симуляция ошибки соединения
            if (annotation.simulateConnectionError()) {
                stubFor(get(urlPathEqualTo("/events"))
                        .willReturn(aResponse()
                                .withStatus(503)
                                .withBody("Service Unavailable")
                        )
                );
                System.out.println("⚠️ WireMock configured with CONNECTION ERROR simulation (503)");
                return; // Не настраиваем успешный ответ
            }

            // 1.3 Симуляция HTTP ошибки
            if (annotation.errorHttpStatus() > 0) {
                stubFor(get(urlPathEqualTo("/events"))
                        .willReturn(aResponse()
                                .withStatus(annotation.errorHttpStatus())
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"error\": \"Service error\"}")
                        )
                );
                System.out.println("⚠️ WireMock configured with HTTP ERROR: " + annotation.errorHttpStatus());
                return; // Не настраиваем успешный ответ
            }

            // ========== 2. Успешный ответ (по умолчанию) ==========

            FraudCheckResponse response = createFraudCheckResponse(annotation);
            String responseJson = objectMapper.writeValueAsString(response);

            // 2.1 Основная заглушка для /events
            stubFor(get(urlPathEqualTo("/events"))
                    .willReturn(aResponse()
                            .withStatus(annotation.httpStatus())
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                            .withFixedDelay((int) annotation.delayMs())
                    )
            );

            // 2.2 Для обратной совместимости - POST /fraud-check
            stubFor(post(urlPathEqualTo("/fraud-check"))
                    .willReturn(aResponse()
                            .withStatus(annotation.httpStatus())
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                            .withFixedDelay((int) annotation.delayMs())
                    )
            );

            System.out.println("WireMock stubs configured for: GET /events and POST /fraud-check");

        } catch (Exception e) {
            throw new RuntimeException("Failed to setup FraudCheckMock", e);
        }
    }

    private FraudCheckResponse createFraudCheckResponse(FraudCheckMock annotation) {
        FraudCheckResponse response = new FraudCheckResponse(
                annotation.status(),
                annotation.decision(),
                annotation.riskScore(),
                annotation.reason(),
                annotation.requiresManualReview(),
                annotation.additionalVerificationRequired()
        );

        response.setTimestamp(LocalDateTime.now());
        response.setTransactionId(UUID.randomUUID().toString());

        return response;
    }

    private void cleanupAfterTest() {
        long threadId = Thread.currentThread().threadId();

        try {
            WireMock.reset();
            System.out.println("WireMock fully reset for thread: " + threadId);
        } catch (Exception e) {
            System.err.println("Error during WireMock reset: " + e.getMessage());
        } finally {
            threadConfigs.remove(threadId);
            currentThreadConfig.remove();
            System.out.println("Cleaned up thread configuration for thread: " + threadId);
        }
    }

    public static FraudCheckMock getCurrentThreadConfig() {
        return currentThreadConfig.get();
    }
}