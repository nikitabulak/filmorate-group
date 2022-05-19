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
    private Long id = 0L;

    private Long generateId() {
        return ++id;
    }

    @Override
    public User addUser(User user) {
        if (user.getLogin().contains(" "))
            throw new ru.yandex.practicum.filmorate.exception.ValidationException("Логин не должен содержать пробелы");
        Long id = generateId();
        user.setId(id);
        users.put(id, user);
        log.info("Добавлен новый пользователь: {}", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        Long id = user.getId();
        if (!users.containsKey(id))
            throw new UserNotFoundException(String.format("Пользователь с id %d не существует", id));
        users.put(user.getId(), user);
        log.info("Данные пользователя {} успешно обновлены", user);
        return user;
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User deleteUser(User user) {
        if (users.containsKey(user.getId())) return users.remove(user.getId());
        else throw new UserNotFoundException(String.format("Пользователь с id %d не найден", id));
    }

    @Override
    public User getUserById(Long id) {
        if (!users.containsKey(id))
            throw new UserNotFoundException(String.format("Пользователь с id %d не найден", id));
        return users.get(id);
    }
}
