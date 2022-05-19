package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("inMemoryFilmStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLike(Long id, Long userId) {
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
        filmStorage.getFilmById(id).getLikes().add(userId);
    }

    public void removeLike(Long id, Long userId) {
        if (!filmStorage.getFilmById(id).getLikes().contains(userId))
            throw new UserNotFoundException(String.format("Пользователь с id %d не ставил лайк фильму", userId));
        log.info("Пользователь {} удалил лайк к фильму {}", userId, id);
        filmStorage.getFilmById(id).getLikes().remove(userId);
    }

    public List<Film> getFilmsByRating(int count) {
        return filmStorage.getFilms().stream()
                .sorted((x1, x2) -> (x2.getRating() - x1.getRating()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
