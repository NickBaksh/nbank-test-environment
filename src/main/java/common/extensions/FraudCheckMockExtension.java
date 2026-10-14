package common.extensions;

import api.models.dto_model.FraudCheckResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import api.configs.Config;
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

    /**
     * Явный клиент к админке WireMock.
     * Статические stubFor()/reset() используют клиент из ThreadLocal, который по умолчанию
     * смотрит на localhost:8080 — в Docker это сам контейнер, а не ваш компьютер.
     */
    private static final WireMock WIRE_MOCK = new WireMock(
            Config.getWireMockHost(),
            Config.getWireMockPort()
    );

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
            if (annotation.simulateTimeout()) {
                WIRE_MOCK.register(post(urlPathEqualTo("/fraud-check"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withFixedDelay(10000)
                        )
                );
                WIRE_MOCK.register(get(urlPathEqualTo("/events"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withFixedDelay(10000)
                        )
                );
                System.out.println("⚠️ WireMock configured with TIMEOUT simulation (10s delay)");
                return;
            }

            if (annotation.simulateConnectionError()) {
                WIRE_MOCK.register(post(urlPathEqualTo("/fraud-check"))
                        .willReturn(aResponse()
                                .withStatus(503)
                                .withBody("Service Unavailable")
                        )
                );
                WIRE_MOCK.register(get(urlPathEqualTo("/events"))
                        .willReturn(aResponse()
                                .withStatus(503)
                                .withBody("Service Unavailable")
                        )
                );
                System.out.println("⚠️ WireMock configured with CONNECTION ERROR simulation (503)");
                return;
            }

            if (annotation.errorHttpStatus() > 0) {
                int status = annotation.errorHttpStatus();
                WIRE_MOCK.register(post(urlPathEqualTo("/fraud-check"))
                        .willReturn(aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"error\": \"Service error\"}")
                        )
                );
                WIRE_MOCK.register(get(urlPathEqualTo("/events"))
                        .willReturn(aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"error\": \"Service error\"}")
                        )
                );
                System.out.println("⚠️ WireMock configured with HTTP ERROR: " + status);
                return;
            }

            FraudCheckResponse response = createFraudCheckResponse(annotation);
            String responseJson = objectMapper.writeValueAsString(response);

            WIRE_MOCK.register(get(urlPathEqualTo("/events"))
                    .willReturn(aResponse()
                            .withStatus(annotation.httpStatus())
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                            .withFixedDelay((int) annotation.delayMs())
                    )
            );

            WIRE_MOCK.register(post(urlPathEqualTo("/fraud-check"))
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
            WIRE_MOCK.resetMappings();
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