package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private Long id = 0L;
    private static final LocalDate releaseDate = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(@Qualifier("inMemoryFilmStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    private Long generateId() {
        return ++id;
    }

    public Film addFilm(Film film) {
        if (film.getReleaseDate().isBefore(releaseDate))
            throw new ValidationException("Attempt to add film " +
                    "with releaseDate before 28-12-1895");
        film.setId(generateId());
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public void addLike(Long id, Long userId) {
        log.info("User id = {} set like film id = {}", userId, id);
        filmStorage.getFilmById(id).addLikeFromUser(userId);
    }

    public void removeLike(Long id, Long userId) {
        if (!filmStorage.getFilmById(id).hasLikeFromUser(userId))
            throw new UserNotFoundException(String.format("User id = %d trying to delete like to film id = %d, " +
                    "which is absent", userId, id));
        log.info("User id = {} deleted like to film id = {}", userId, id);
        filmStorage.getFilmById(id).removeLikeFromUser(userId);
    }

    public List<Film> getFilmsByRating(int count) {
        return filmStorage.getFilms().stream()
                .sorted((x1, x2) -> (x2.getRating() - x1.getRating()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
