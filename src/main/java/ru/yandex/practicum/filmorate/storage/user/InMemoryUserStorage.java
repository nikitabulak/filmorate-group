package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        users.put(user.getId(), user);
        log.info("New user added: {}", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        Long id = user.getId();
        if (!users.containsKey(id))
            throw new UserNotFoundException(String.format("Attempt to update user with " +
                    "absent id = %d", id));
        users.put(user.getId(), user);
        log.info("User {} has been successfully updated", user);
        return user;
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User deleteUser(User user) {
        if (users.containsKey(user.getId())) return users.remove(user.getId());
        else throw new UserNotFoundException(String.format("Attempt to delete user with " +
                "absent id = %d", user.getId()));
    }

    @Override
    public User getUserById(Long id) {
        if (!users.containsKey(id))
            throw new UserNotFoundException(String.format("Request user with absent id = %d", id));
        return users.get(id);
    }
}
