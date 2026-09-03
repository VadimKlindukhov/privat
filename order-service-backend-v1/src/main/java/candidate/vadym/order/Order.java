package candidate.vadym.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Order {

    private UUID id;
    private UUID clientId;
    private BigDecimal amount;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public Order() {
    }

    public Order(UUID id, UUID clientId, BigDecimal amount, OrderStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Order create(UUID clientId, BigDecimal amount) {
        Instant now = Instant.now();
        return new Order(UUID.randomUUID(), clientId, amount, OrderStatus.NEW, now, now);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
