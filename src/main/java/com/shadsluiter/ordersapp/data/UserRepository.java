package com.shadsluiter.ordersapp.data;

import com.shadsluiter.ordersapp.models.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class UserRepository implements UserRepositoryInterface {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserEntity findByLoginName(String loginName) {

        String sql = """
            SELECT u.*, r.role
            FROM users u
            LEFT JOIN roles r ON u.id = r.user_id
            WHERE u.login_name = ?
            """;

        try {
            List<UserEntity> users = jdbcTemplate.query(sql, new UserWithRolesExtractor(), loginName);
            return users.isEmpty() ? null : users.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<UserEntity> findAll() {
        String sql = "SELECT u.*, r.role FROM users u LEFT JOIN roles r ON u.id = r.user_id";
        return jdbcTemplate.query(sql, new UserWithRolesExtractor());
    }

    @Override
    public void deleteById(Long id) {
        // (Optional best practice) delete roles first to avoid FK issues if cascade is not enabled
        jdbcTemplate.update("DELETE FROM roles WHERE user_id = ?", id);
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public UserEntity save(UserEntity userEntity) {

        // new user
        if (userEntity.getId() == null) {

            // keep your teaching-mode defaults
            if (userEntity.getRoles() == null) {
                userEntity.setRoles(new HashSet<>(Arrays.asList("ROLE_USER", "ROLE_ADMIN")));
            } else {
                userEntity.getRoles().add("ROLE_USER");
            }

            String sql = """
                INSERT INTO users (login_name, password, enabled, account_non_expired, credentials_non_expired, account_non_locked)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, userEntity.getUserName());
                ps.setString(2, userEntity.getPassword());
                ps.setBoolean(3, userEntity.isEnabled());
                ps.setBoolean(4, userEntity.isAccountNonExpired());
                ps.setBoolean(5, userEntity.isCredentialsNonExpired());
                ps.setBoolean(6, userEntity.isAccountNonLocked());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key == null) {
                throw new IllegalStateException("Failed to retrieve generated user id.");
            }
            userEntity.setId(key.longValue());

        } else {

            String sql = """
                UPDATE users
                SET login_name = ?,
                    password = ?,
                    enabled = ?,
                    account_non_expired = ?,
                    credentials_non_expired = ?,
                    account_non_locked = ?
                WHERE id = ?
                """;

            jdbcTemplate.update(
                    sql,
                    userEntity.getUserName(),
                    userEntity.getPassword(),
                    userEntity.isEnabled(),
                    userEntity.isAccountNonExpired(),
                    userEntity.isCredentialsNonExpired(),
                    userEntity.isAccountNonLocked(),
                    userEntity.getId()
            );
        }

        // replace-all roles strategy (keep your approach)
        deleteRoles(userEntity);
        saveRoles(userEntity);

        return userEntity;
    }

    public void saveRoles(UserEntity userEntity) {
        if (userEntity.getRoles() == null) return;

        String sql = "INSERT INTO roles (user_id, role) VALUES (?, ?)";

        for (String role : userEntity.getRoles()) {
            jdbcTemplate.update(sql, userEntity.getId(), role);
        }
    }

    public void deleteRoles(UserEntity userEntity) {
        jdbcTemplate.update("DELETE FROM roles WHERE user_id = ?", userEntity.getId());
    }

    @Override
    public UserEntity findById(Long id) {

        String sql = """
            SELECT u.*, r.role
            FROM users u
            LEFT JOIN roles r ON u.id = r.user_id
            WHERE u.id = ?
            """;

        try {
            List<UserEntity> users = jdbcTemplate.query(sql, new UserWithRolesExtractor(), id);
            return users.isEmpty() ? null : users.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public long count() {
        Long result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        return result != null ? result : 0;
    }

    @Override
    public void delete(UserEntity user) {
        deleteById(user.getId());
    }

    @Override
    public void deleteAll() {
        // consistent order if FK exists
        jdbcTemplate.update("DELETE FROM roles");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Override
    public void deleteAll(Iterable<? extends UserEntity> users) {
        for (UserEntity user : users) {
            delete(user);
        }
    }

    @Override
    public List<UserEntity> saveAll(Iterable<UserEntity> users) {
        for (UserEntity user : users) {
            save(user);
        }
        return (List<UserEntity>) users;
    }

    private static class UserWithRolesExtractor implements ResultSetExtractor<List<UserEntity>> {

        @Override
        public List<UserEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, UserEntity> userMap = new HashMap<>();

            while (rs.next()) {
                Long id = rs.getLong("id");
                UserEntity user = userMap.get(id);

                if (user == null) {
                    user = new UserEntity();
                    user.setId(id);
                    user.setUserName(rs.getString("login_name"));
                    user.setPassword(rs.getString("password"));
                    user.setEnabled(rs.getBoolean("enabled"));
                    user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                    user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
                    user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                    user.setRoles(new HashSet<>());
                    userMap.put(id, user);
                }

                String role = rs.getString("role");
                if (role != null) {
                    user.getRoles().add(role);
                }
            }

            return new ArrayList<>(userMap.values());
        }
    }
}
