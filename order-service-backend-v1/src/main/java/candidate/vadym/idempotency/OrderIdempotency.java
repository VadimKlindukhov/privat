package candidate.vadym.idempotency;

import java.time.Instant;
import java.util.UUID;

public class OrderIdempotency {

    private UUID id;
    private UUID orderId;
    private Instant createdAt;

    public OrderIdempotency() {
    }

    public OrderIdempotency(UUID id, UUID orderId, Instant createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.createdAt = createdAt;
    }

    /** New claim row: the key is reserved, but no order exists for it yet. */
    public static OrderIdempotency claim(UUID idempotencyKey) {
        return new OrderIdempotency(idempotencyKey, null, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
