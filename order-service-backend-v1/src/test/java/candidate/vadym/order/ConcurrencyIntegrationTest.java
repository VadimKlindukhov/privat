package candidate.vadym.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import candidate.vadym.AbstractIntegrationTest;
import candidate.vadym.limit.OrderDailyUsageRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Both required concurrency scenarios live in a single test class (and therefore a single Spring context / single
 * set of Testcontainers) so a mid-suite context reload can never point a background component (e.g. the
 * order.placed RabbitMQ consumer) at a container a later test class has already replaced.
 */
class ConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDailyUsageRepository orderDailyUsageRepository;

    /**
     * 30 threads race to create orders for the same client at the same time. The 50 000 UAH/day limit allows
     * exactly 25 of them through (25 x 2000.00 == 50000.00); every request must still get a plain 200 (the
     * limit-exceeded case is recorded on the order itself, not surfaced as an HTTP error) and, critically, the
     * amount actually reserved in orders_daily_usage must never exceed the limit — that row's FOR UPDATE lock
     * is the thing this test is really checking.
     */
    @Test
    void concurrentOrdersNeverExceedDailyLimit() throws InterruptedException {
        int threadCount = 30;
        BigDecimal orderAmount = new BigDecimal("2000.00");
        int expectedAccepted = 25;

        UUID clientId = UUID.randomUUID();

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        List<HttpStatusCode> statuses = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    statuses.add(submitOrder(clientId, orderAmount, UUID.randomUUID()));
                } catch (Exception e) {
                    statuses.add(HttpStatus.INTERNAL_SERVER_ERROR);
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        boolean finished = done.await(60, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(finished).as("all requests completed within the timeout (no deadlock)").isTrue();
        assertThat(statuses).hasSize(threadCount);
        assertThat(statuses).as("no request should fail with a server error: %s", statuses)
                .allMatch(status -> !status.is5xxServerError());

        List<Order> clientOrders = orderRepository.findAll().stream()
                .filter(o -> o.getClientId().equals(clientId))
                .toList();
        assertThat(clientOrders)
                .as("every accepted claim inserts exactly one order row, whether or not it later exceeded the limit")
                .hasSize(threadCount);

        BigDecimal reserved = orderDailyUsageRepository.findForUpdate(clientId, LocalDate.now()).getAmount();
        assertThat(reserved)
                .as("only the orders that fit under the limit may be reserved in the daily usage total")
                .isEqualByComparingTo(orderAmount.multiply(BigDecimal.valueOf(expectedAccepted)));
    }

    /**
     * 20 threads submit the exact same request body with the exact same X-Idempotency-Key at the same time.
     * Exactly one order must be persisted and none may fail with a 500 — the second and later inserts into
     * orders_idempotency block on the first row's unique index until that transaction resolves, then either
     * fail cleanly (duplicate key, caught in the controller) or proceed (if the first one rolled back).
     */
    @Test
    void concurrentRequestsWithSameIdempotencyKeyCreateExactlyOneOrder() throws InterruptedException {
        int threadCount = 20;
        UUID clientId = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("1500.00");

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        List<HttpStatusCode> statuses = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    statuses.add(submitOrder(clientId, amount, idempotencyKey));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        boolean finished = done.await(60, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(finished).as("all requests completed within the timeout (no deadlock)").isTrue();
        assertThat(statuses).hasSize(threadCount);
        assertThat(statuses).as("no request should fail with a server error: %s", statuses)
                .allMatch(status -> !status.is5xxServerError());

        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getClientId().equals(clientId))
                .toList();
        assertThat(orders).as("exactly one order row must be persisted for this idempotency key").hasSize(1);
    }

    private HttpStatusCode submitOrder(UUID clientId, BigDecimal amount, UUID idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Idempotency-Key", idempotencyKey.toString());

        Map<String, Object> body = Map.of("clientId", clientId.toString(), "amount", amount);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        return restTemplate.postForEntity("/api/v1/orders", entity, String.class).getStatusCode();
    }

}
