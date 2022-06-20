package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Component("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User add(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("user_id");
        user.setId(simpleJdbcInsert.executeAndReturnKey(user.toMap()).longValue());
        log.info("New user added: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        if (isUserExists(user.getId())) {
            String sqlQuery = "UPDATE USERS SET " +
                    "email = ?, login = ?, name = ?, birthday = ? " +
                    "WHERE user_id = ?";
            jdbcTemplate.update(sqlQuery,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    user.getId());
            log.info("User {} has been successfully updated", user);
            return user;
        } else {
            throw new UserNotFoundException(String.format("Attempt to update user with " +
                    "absent id = %d", user.getId()));
        }
    }

    @Override
    public Collection<User> getAll() {
        String sql = "SELECT * FROM USERS ";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                rs.getLong("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate())
        );
    }

    @Override
    public User delete(User user) {
        if (isUserExists(user.getId())) {
            String sql = "DELETE FROM USERS WHERE user_id = ?";
            jdbcTemplate.update(sql, user.getId());
            return user;
        } else throw new UserNotFoundException(String.format("Attempt to delete user with " +
                "absent id = %d", user.getId()));
    }

    @Override
    public Optional<User> getById(Long id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE user_id = ?", id);
        if (userRows.first()) {
            User user = new User(
                    userRows.getLong("user_id"),
                    userRows.getString("email"),
                    userRows.getString("login"),
                    userRows.getString("name"),
                    userRows.getDate("birthday").toLocalDate()
                    );
            log.info("Found user id = {}", id);
            return Optional.of(user);
        } else {
           return Optional.empty();
        }
    }

    public boolean isUserExists(Long id) {
        String sql = "SELECT * FROM USERS WHERE user_id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, id);
        return userRows.first();
    }
}

