package candidate.vadym.limit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OrderDailyUsageRepository {

    private static final RowMapper<OrderDailyUsage> ROW_MAPPER = (rs, rowNum) -> new OrderDailyUsage(
            UUID.fromString(rs.getString("client_id")),
            rs.getDate("date").toLocalDate(),
            rs.getBigDecimal("amount"));

    private final JdbcTemplate jdbcTemplate;

    public OrderDailyUsageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void ensureRowExists(UUID clientId, LocalDate date) {
        jdbcTemplate.update(
                "insert into orders_daily_usage (client_id, date, amount) values (?, ?, 0) on conflict (client_id, date) do nothing",
                clientId, date);
    }

    public OrderDailyUsage findForUpdate(UUID clientId, LocalDate date) {
        return jdbcTemplate.queryForObject(
                "select * from orders_daily_usage where client_id = ? and date = ? for update",
                ROW_MAPPER, clientId, date);
    }

    public void updateAmount(UUID clientId, LocalDate date, BigDecimal amount) {
        jdbcTemplate.update(
                "update orders_daily_usage set amount = ? where client_id = ? and date = ?",
                amount, clientId, date);
    }
}
