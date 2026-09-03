package candidate.vadym.provider;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProviderService {

    private static final double FAILURE_THRESHOLD = 0.25;
    private static final double TIMEOUT_THRESHOLD = 0.4;

    private final RetryTemplate retryTemplate = RetryTemplate.builder()
            .maxAttempts(3)
            .fixedBackoff(Duration.ofMillis(300))
            .notRetryOn(ProviderTimeoutException.class)
            .build();

    public void sendOrder(UUID uuid, BigDecimal amount) {
        retryTemplate.execute(context -> {
            simulateResponse(uuid, amount);
            return null;
        });
    }

    private void simulateResponse(UUID uuid, BigDecimal amount) {
        double random = Math.random();

        if (random < FAILURE_THRESHOLD) {
            throw new RuntimeException();
        } else if (random < TIMEOUT_THRESHOLD) {
            try {
                Thread.sleep(Duration.of(10L, ChronoUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            throw new ProviderTimeoutException();
        }
    }
}
