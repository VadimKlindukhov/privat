package candidate.vadym.limit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class OrderDailyUsage {

    private UUID clientId;
    private LocalDate date;
    private BigDecimal amount;

    public OrderDailyUsage() {
    }

    public OrderDailyUsage(UUID clientId, LocalDate date, BigDecimal amount) {
        this.clientId = clientId;
        this.date = date;
        this.amount = amount;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
