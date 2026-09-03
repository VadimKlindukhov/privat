package candidate.vadym.event;

import candidate.vadym.order.OrderStatus;

import java.util.UUID;

public record OrderStatusChangedEvent(
        UUID orderId,
        OrderStatus status) {
}
