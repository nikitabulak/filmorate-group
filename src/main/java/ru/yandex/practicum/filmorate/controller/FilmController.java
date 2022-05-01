package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@Validated
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 0;
    private static final LocalDate releaseDate = LocalDate.of(1895, 12, 28);

    private int generateId() {
        return ++id;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(releaseDate))
            throw new ru.yandex.practicum.filmorate.exception.ValidationException("Дата релиза раньше releaseDate");
        int id = generateId();
        film.setId(id);
        films.put(id, film);
        log.info("Добавлен новый фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film saveUser(@Valid @RequestBody Film film) {
        int id = film.getId();
        if (id < 0)
            throw new ru.yandex.practicum.filmorate.exception.ValidationException("PUT: отрицательный id " + id);
        if (!films.containsKey(id))
            throw new ru.yandex.practicum.filmorate.exception.ValidationException("PUT: несуществующий id" + id);
        if (id == 0) {
            id = generateId();
            film.setId(id);
            films.put(id, film);
            log.info("Добавлен новый фильм {}", film);
        } else {
            films.put(film.getId(), film);
            log.info("Фильм {} успешно обновлен", film);
        }
        return film;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Ошибка валидации: " + e.getMessage());
        return new ResponseEntity<>("not valid due to validation error: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(ru.yandex.practicum.filmorate.exception.ValidationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleValidationException
            (ru.yandex.practicum.filmorate.exception.ValidationException e) {
        log.warn("Ошибка валидации: " + e.getMessage());
        return new ResponseEntity<>("not valid due to validation error: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
