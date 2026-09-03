package candidate.vadym.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderPlacedEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(OrderPlacedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.ORDER_PLACED_QUEUE, event);
    }
}
