package candidate.vadym.event;

import candidate.vadym.messaging.OrderPlacedEvent;
import candidate.vadym.messaging.OrderPlacedEventPublisher;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderCreationTransactionListener {

    private final OrderPlacedEventPublisher orderPlacedEventPublisher;

    public OrderCreationTransactionListener(OrderPlacedEventPublisher orderPlacedEventPublisher) {
        this.orderPlacedEventPublisher = orderPlacedEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        orderPlacedEventPublisher.publish(new OrderPlacedEvent(event.orderId(), event.amount()));
    }
}
