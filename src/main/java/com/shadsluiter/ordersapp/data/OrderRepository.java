package com.shadsluiter.ordersapp.data;

import com.shadsluiter.ordersapp.models.OrderEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class OrderRepository implements OrderRepositoryInterface {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<OrderEntity> orderRowMapper = (rs, rowNum) -> {
        OrderEntity order = new OrderEntity();
        order.setId(rs.getLong("id"));
        order.setDate(rs.getDate("date")); // returns java.sql.Date (OK)
        // Store as String in entity for Mongo readiness
        order.setCustomerid(String.valueOf(rs.getLong("customerid")));
        order.setNotes(rs.getString("notes"));
        order.setPictureUrl(rs.getString("pictureUrl"));
        return order;
    };

    @Override
    public List<OrderEntity> findAll() {
        String sql = "SELECT id, date, customerid, notes, pictureUrl FROM orders";
        return jdbcTemplate.query(sql, orderRowMapper);
    }

    @Override
    public List<OrderEntity> findByCustomerid(Long customerid) {
        String sql = "SELECT id, date, customerid, notes, pictureUrl FROM orders WHERE customerid = ?";
        return jdbcTemplate.query(sql, orderRowMapper, customerid);
    }

    @Override
    public OrderEntity findById(Long id) {
        String sql = "SELECT id, date, customerid, notes, pictureUrl FROM orders WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, orderRowMapper, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM orders WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<OrderEntity> findByNote(String name) {
        String sql = "SELECT id, date, customerid, notes, pictureUrl FROM orders WHERE notes LIKE ?";
        return jdbcTemplate.query(sql, orderRowMapper, "%" + name + "%");
    }

    @Override
    public OrderEntity save(OrderEntity order) {
        if (order == null) {
            throw new IllegalArgumentException("OrderEntity cannot be null");
        }
        if (order.getDate() == null) {
            throw new IllegalArgumentException("Order date cannot be null");
        }

        // Normalize date for JDBC
        java.sql.Date sqlDate = new java.sql.Date(order.getDate().getTime());

        // SQL schema uses numeric customerid today, entity stores String for Mongo later
        Long customerIdAsLong;
        try {
            customerIdAsLong = Long.parseLong(order.getCustomerid());
        } catch (Exception ex) {
            throw new IllegalArgumentException("customerid must be numeric for SQL storage: " + order.getCustomerid(), ex);
        }

        if (order.getId() == null || order.getId() <= 0) {
            // INSERT
            String sql = "INSERT INTO orders (date, customerid, notes, pictureUrl) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setDate(1, sqlDate);
                ps.setLong(2, customerIdAsLong);
                ps.setString(3, order.getNotes());
                ps.setString(4, order.getPictureUrl());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key == null) {
                throw new IllegalStateException("Insert succeeded but no generated key returned.");
            }

            order.setId(key.longValue());
            // Keep Mongo-ready string customerid already set
            return order;

        } else {
            // UPDATE
            String sql = "UPDATE orders SET date = ?, customerid = ?, notes = ?, pictureUrl = ? WHERE id = ?";
            jdbcTemplate.update(
                    sql,
                    sqlDate,
                    customerIdAsLong,          // bind as numeric
                    order.getNotes(),
                    order.getPictureUrl(),
                    order.getId()
            );
            return order;
        }
    }

    // --- Interface methods you added but haven't implemented yet ---
    // Leaving these as runtime exceptions is risky. If you don't use them, remove them from the interface.
    // If you DO want them, implement them properly below.

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM orders";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count == null ? 0L : count;
    }

    @Override
    public void delete(OrderEntity order) {
        if (order == null || order.getId() == null) return;
        deleteById(order.getId());
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM orders";
        jdbcTemplate.update(sql);
    }

    @Override
    public void deleteAll(Iterable<? extends OrderEntity> orders) {
        if (orders == null) return;
        for (OrderEntity o : orders) {
            if (o != null && o.getId() != null) {
                deleteById(o.getId());
            }
        }
    }

    @Override
    public List<OrderEntity> saveAll(Iterable<OrderEntity> orders) {
        if (orders == null) return List.of();
        // Simple teaching-friendly implementation: save sequentially then return fresh list
        for (OrderEntity o : orders) {
            save(o);
        }
        return findAll();
    }
}
