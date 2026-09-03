package candidate.vadym.messaging;

import candidate.vadym.order.OrderStatus;

import java.util.UUID;

public record OrderUpdatedEvent(
        UUID orderId,
        OrderStatus status) {
}
