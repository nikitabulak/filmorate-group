package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int id = 0;

    private int generateId() {
        return ++id;
    }

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public void create(@Valid @RequestBody User user) {
        int id = generateId();
        user.setId(id);
        users.put(id, user);
        log.info("Добавлен новый пользователь: {}", user);
    }

    @PutMapping
    public void saveUser(@Valid @RequestBody User user) {
        int id = user.getId();
        if (id < 0) throw new ValidationException("PUT: отрицательный id " + id);
        if (!users.containsKey(id)) throw new ValidationException("PUT: несуществующий id" + id);
        if (id == 0) {
            id = generateId();
            user.setId(id);
            users.put(id, user);
            log.info("Добавлен новый пользователь {}", user);
        } else {
            users.put(user.getId(), user);
            log.info("Данные пользователя {} успешно обновлены", user);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Ошибка валидации: " + e.getMessage());
        return new ResponseEntity<>("not valid due to validation error: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
