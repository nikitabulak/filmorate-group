package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private static final String UPDATE_USER = "UPDATE USERS SET " +
            "email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE user_id = ?";
    private static final String GET_USER_BY_ID = "SELECT * FROM USERS WHERE user_id = ?";
    private static final String DELETE_USER = "DELETE FROM USERS WHERE user_id = ?";
    private static final String GET_ALL_USERS = "SELECT * FROM USERS ";

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
            jdbcTemplate.update(UPDATE_USER,
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
        return jdbcTemplate.query(GET_ALL_USERS, new UserJdbcMapper());
    }

    @Override
    public User delete(User user) {
        if (isUserExists(user.getId())) {
            jdbcTemplate.update(DELETE_USER, user.getId());
            return user;
        } else throw new UserNotFoundException(String.format("Attempt to delete user with " +
                "absent id = %d", user.getId()));
    }

    @Override
    public Optional<User> getById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(GET_USER_BY_ID, new UserJdbcMapper(), id);
            log.info("Found user id = {}", id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean isUserExists(Long id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(GET_USER_BY_ID, id);
        return userRows.first();
    }
}

