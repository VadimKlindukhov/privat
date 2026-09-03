package candidate.vadym.history;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import candidate.vadym.order.OrderStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OrderStatusHistoryRepository {

    private static final RowMapper<OrderStatusHistory> ROW_MAPPER = (rs, rowNum) -> new OrderStatusHistory(
            UUID.fromString(rs.getString("order_id")),
            OrderStatus.valueOf(rs.getString("status")),
            rs.getString("note"),
            rs.getTimestamp("date_time").toInstant());

    private final JdbcTemplate jdbcTemplate;

    public OrderStatusHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(OrderStatusHistory entry) {
        jdbcTemplate.update(
                "insert into orders_status_history (order_id, status, note, date_time) values (?, ?, ?, ?)",
                entry.getOrderId(), entry.getStatus().name(), entry.getNote(), Timestamp.from(entry.getDateTime()));
    }

    public List<OrderStatusHistory> findByOrderId(UUID orderId) {
        return jdbcTemplate.query("select * from orders_status_history where order_id = ?", ROW_MAPPER, orderId);
    }
}
