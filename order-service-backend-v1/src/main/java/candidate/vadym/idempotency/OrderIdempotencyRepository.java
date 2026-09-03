package candidate.vadym.idempotency;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OrderIdempotencyRepository {

    private static final RowMapper<OrderIdempotency> ROW_MAPPER = (rs, rowNum) -> {
        String orderId = rs.getString("order_id");
        return new OrderIdempotency(
                UUID.fromString(rs.getString("id")),
                orderId != null ? UUID.fromString(orderId) : null,
                rs.getTimestamp("created_at").toInstant());
    };

    private final JdbcTemplate jdbcTemplate;

    public OrderIdempotencyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Claims the key by inserting a row with {@code order_id = NULL}, before the order exists.
     * A concurrent call with the same key blocks here until this transaction commits or rolls back, then
     * either fails with {@link org.springframework.dao.DuplicateKeyException} (this transaction committed —
     * the caller should look the existing order up via {@link #findById}) or proceeds normally (this
     * transaction rolled back, so the key was never actually taken).
     */
    public void claim(UUID id) {
        jdbcTemplate.update("insert into orders_idempotency (id, created_at) values (?, ?)", id, Timestamp.from(Instant.now()));
    }

    /** Backfills the order id onto an already-claimed key, in the same transaction as the claim. */
    public void attachOrder(UUID idempotencyKey, UUID orderId) {
        jdbcTemplate.update("update orders_idempotency set order_id = ? where id = ?", orderId, idempotencyKey);
    }

    public OrderIdempotency findById(UUID id) {
        return jdbcTemplate.queryForObject("select * from orders_idempotency where id = ?", ROW_MAPPER, id);
    }
}
