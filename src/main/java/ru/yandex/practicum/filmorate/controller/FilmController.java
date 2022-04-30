package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.*;

@Slf4j
@RestController
@Validated
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 0;

    private int generateId() {
        return ++id;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public void create(@Valid @RequestBody Film film) {
        log.info("Добавлен новый фильм: {}", film);
        int id = generateId();
        film.setId(id);
        films.put(id, film);
    }

    @PutMapping
    public void saveUser(@Valid @RequestBody Film film) {
        int id = film.getId();
        if (id < 0) throw new ValidationException("PUT: отрицательный id " + id);
        if (!films.containsKey(id)) throw new ValidationException("PUT: несуществующий id" + id);
        if (id == 0) {
            id = generateId();
            film.setId(id);
            films.put(id, film);
            log.info("Добавлен новый фильм {}", film);
        } else {
            films.put(film.getId(), film);
            log.info("Фильм {} успешно обновлен", film);
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
