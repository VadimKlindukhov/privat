package candidate.vadym.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String ORDER_PLACED_QUEUE = "order.placed";
    public static final String ORDER_UPDATED_QUEUE = "order.updated";

    @Bean
    public Queue orderPlacedQueue() {
        return new Queue(ORDER_PLACED_QUEUE, true);
    }

    @Bean
    public Queue orderUpdatedQueue() {
        return new Queue(ORDER_UPDATED_QUEUE, true);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
