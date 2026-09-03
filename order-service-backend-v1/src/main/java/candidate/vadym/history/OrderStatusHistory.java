package candidate.vadym.history;

import candidate.vadym.order.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public class OrderStatusHistory {

    private UUID orderId;
    private OrderStatus status;
    private String note;
    private Instant dateTime;

    public OrderStatusHistory() {
    }

    public OrderStatusHistory(UUID orderId, OrderStatus status, String note, Instant dateTime) {
        this.orderId = orderId;
        this.status = status;
        this.note = note;
        this.dateTime = dateTime;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    public void setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
    }
}
