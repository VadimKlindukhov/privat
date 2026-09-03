package candidate.vadym.order;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderFacade orderFacade;

    public OrderController(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @PostMapping
    public void createOrder(
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request) {
        orderFacade.placeOrder(idempotencyKey, request.clientId(), request.amount());
    }

    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable UUID id) {
        return orderFacade.getOrder(id);
    }

    @GetMapping
    public List<OrderDto> getOrders() {
        return orderFacade.getAllOrders();
    }
}
