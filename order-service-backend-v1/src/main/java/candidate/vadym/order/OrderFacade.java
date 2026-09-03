package candidate.vadym.order;

import candidate.vadym.event.OrderCreatedEvent;
import candidate.vadym.event.OrderStatusChangedEvent;
import candidate.vadym.history.OrderStatusHistory;
import candidate.vadym.history.OrderStatusHistoryRepository;
import candidate.vadym.idempotency.OrderIdempotencyRepository;
import candidate.vadym.limit.OrderDailyUsage;
import candidate.vadym.limit.OrderDailyUsageRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class OrderFacade {

    private final OrderRepository orderRepository;
    private final OrderDailyUsageRepository orderDailyUsageRepository;
    private final OrderIdempotencyRepository orderIdempotencyRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderFacade(OrderRepository orderRepository, OrderDailyUsageRepository orderDailyUsageRepository, OrderIdempotencyRepository orderIdempotencyRepository, OrderStatusHistoryRepository orderStatusHistoryRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderDailyUsageRepository = orderDailyUsageRepository;
        this.orderIdempotencyRepository = orderIdempotencyRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public void placeOrder(UUID idempotencyId, UUID clientId, BigDecimal amount) {
        try {
            orderIdempotencyRepository.claim(idempotencyId);
        } catch (DuplicateKeyException e) {
            return;
        }

        Order order = Order.create(clientId, amount);
        orderRepository.insert(order);
        orderIdempotencyRepository.attachOrder(idempotencyId, order.getId());

        LocalDate currentDate = LocalDate.now();
        orderDailyUsageRepository.ensureRowExists(clientId, currentDate);
        OrderDailyUsage orderDailyUsage = orderDailyUsageRepository.findForUpdate(clientId, currentDate);
        BigDecimal usage = orderDailyUsage.getAmount().add(amount);
        if (usage.compareTo(BigDecimal.valueOf(50_000L)) > 0) {
            updateStatus(order.getId(), OrderStatus.FAILED, "Перевищено щоденний ліміт");
            return;
        }

        orderDailyUsageRepository.updateAmount(clientId, currentDate, usage);

        applicationEventPublisher.publishEvent(new OrderCreatedEvent(order.getId(), order.getAmount()));
    }

    @Transactional
    public void updateStatus(UUID orderId, OrderStatus status, String note) {
        OrderStatusHistory statusHistory = new OrderStatusHistory();
        statusHistory.setOrderId(orderId);
        statusHistory.setStatus(status);
        statusHistory.setNote(note);
        statusHistory.setDateTime(Instant.now());
        orderStatusHistoryRepository.insert(statusHistory);

        orderRepository.updateStatus(orderId, status);
        applicationEventPublisher.publishEvent(new OrderStatusChangedEvent(orderId, status));
    }

    @Transactional
    public OrderDto getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        List<OrderStatusHistory> statusHistory = orderStatusHistoryRepository.findByOrderId(orderId);

        return new OrderDto(order.getId(), order.getStatus(), order.getAmount(), statusHistory);
    }

    @Transactional
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(e -> new OrderDto(e.getId(), e.getStatus(), e.getAmount(), null))
                .toList();
    }
}
