package candidate.vadym.event;

import candidate.vadym.messaging.OrderUpdatedEvent;
import candidate.vadym.messaging.OrderUpdatedEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderStatusTransactionListener {

    private final OrderUpdatedEventPublisher orderUpdatedEventPublisher;

    public OrderStatusTransactionListener(OrderUpdatedEventPublisher orderUpdatedEventPublisher) {
        this.orderUpdatedEventPublisher = orderUpdatedEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStatusUpdate(OrderStatusChangedEvent event) {
        orderUpdatedEventPublisher.publish(new OrderUpdatedEvent(event.orderId(), event.status()));
    }
}
