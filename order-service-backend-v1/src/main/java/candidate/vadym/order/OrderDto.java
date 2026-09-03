package candidate.vadym.order;

import candidate.vadym.history.OrderStatusHistory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID orderId,
        OrderStatus status,
        BigDecimal amount,
        List<OrderStatusHistory> statusHistory) {
}
