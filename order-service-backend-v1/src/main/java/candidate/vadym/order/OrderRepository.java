package candidate.vadym.order;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    private static final RowMapper<Order> ROW_MAPPER = (rs, rowNum) -> new Order(
            UUID.fromString(rs.getString("id")),
            UUID.fromString(rs.getString("client_id")),
            rs.getBigDecimal("amount"),
            OrderStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("updated_at").toInstant());

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Order order) {
        jdbcTemplate.update(
                "insert into orders (id, client_id, amount, status, created_at, updated_at) values (?, ?, ?, ?, ?, ?)",
                order.getId(), order.getClientId(), order.getAmount(), order.getStatus().name(),
                Timestamp.from(order.getCreatedAt()), Timestamp.from(order.getUpdatedAt()));
    }

    public Optional<Order> findById(UUID id) {
        try {
            return Optional.of(jdbcTemplate.queryForObject("select * from orders where id = ?", ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Order> findAll() {
        return jdbcTemplate.query("select * from orders order by created_at desc", ROW_MAPPER);
    }

    public void updateStatus(UUID id, OrderStatus status) {
        jdbcTemplate.update(
                "update orders set status = ?, updated_at = ? where id = ?",
                status.name(), Timestamp.from(Instant.now()), id);
    }

}
