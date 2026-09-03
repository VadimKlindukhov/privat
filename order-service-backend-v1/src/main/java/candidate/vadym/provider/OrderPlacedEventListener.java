package candidate.vadym.provider;

import candidate.vadym.messaging.OrderPlacedEvent;
import candidate.vadym.messaging.RabbitMqConfig;
import candidate.vadym.order.OrderFacade;
import candidate.vadym.order.OrderStatus;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedEventListener {

    private final ProviderService providerService;
    private final OrderFacade orderFacade;

    public OrderPlacedEventListener(ProviderService providerService, OrderFacade orderFacade) {
        this.providerService = providerService;
        this.orderFacade = orderFacade;
    }

    @RabbitListener(queues = RabbitMqConfig.ORDER_PLACED_QUEUE)
    public void onOrderPlaced(OrderPlacedEvent event) {
        try {
            orderFacade.updateStatus(event.orderId(), OrderStatus.PROCESSING, null);
            providerService.sendOrder(event.orderId(), event.amount());
        } catch (Exception e) {
            orderFacade.updateStatus(event.orderId(), OrderStatus.FAILED, "Не вийшло виконати запит");
            return;
        }
        orderFacade.updateStatus(event.orderId(), OrderStatus.COMPLETED, null);
    }
}
