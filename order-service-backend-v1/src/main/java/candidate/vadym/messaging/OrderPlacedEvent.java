package candidate.vadym.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID orderId,
        BigDecimal amount) {
}
