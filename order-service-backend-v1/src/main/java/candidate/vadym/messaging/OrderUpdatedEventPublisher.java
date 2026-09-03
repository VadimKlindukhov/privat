package candidate.vadym.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderUpdatedEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderUpdatedEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(OrderUpdatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.ORDER_UPDATED_QUEUE, event);
    }
}
