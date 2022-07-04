package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikesStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final LikesStorage likesStorage;
    private Long id = 0L;
    private static final LocalDate releaseDate = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       LikesStorage likesStorage) {
        this.filmStorage = filmStorage;
        this.likesStorage = likesStorage;
    }

    private Long generateId() {
        return ++id;
    }

    public Film addFilm(Film film) {
        if (film.getReleaseDate().isBefore(releaseDate))
            throw new ValidationException("Attempt to add film " +
                    "with releaseDate before 28-12-1895");
        filmStorage.add(film);
        return film;
    }

    public Film updateFilm(Film film) {
        filmStorage.update(film);
        Film filmReturn = filmStorage.getById(film.getId()).orElseThrow(
                () -> new FilmNotFoundException(String.format("Request film with absent id = %d", id)));
        if (film.getGenres() == null) filmReturn.setGenres(null);
        else if (film.getGenres().isEmpty()) filmReturn.setGenres(new HashSet<>());
        if (film.getDirectors() == null) {
            filmReturn.setDirectors(null);
        }             //	insert from Oleg Sharomov
        return filmReturn;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getAll();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new FilmNotFoundException(String.format("Request film with absent id = %d", id)));
    }

    public void deleteFilmById(Long id){
        filmStorage.deleteById(id);
    }

    public void addLike(Long id, Long userId) {
        likesStorage.addLike(id, userId);
        log.info("User id = {} set like film id = {}", userId, id);
    }

    public void removeLike(Long id, Long userId) {
        likesStorage.removeLike(id, userId);
        log.info("User id = {} deleted like to film id = {}", userId, id);
    }

    public List<Film> getFilmsByRating(int count, int genreId, int year) {

        return likesStorage.getPopular(count, genreId, year);
    }


    public List<Film> search(String query, String by) {
        int count = 10;
        int genreId = -1;
        int year = -1;
        if (query == null) {
            return getFilmsByRating(count, genreId, year);
        } else {
            if (by != null) {
                String[] words = by.toLowerCase().replaceAll(" ", "").split(",");
                if (words.length == 1 && words[0].equals("title")) {
                    List<Film> films = filmStorage.searchByTitle(query);
                    return replaceGenresByNull(films);
                }

                if (words.length == 1 && words[0].equals("director")) {
                    List<Film> films = filmStorage.searchByDirector(query);
                    return replaceGenresByNull(films);
                } else if (words.length > 1) {
                    if ((words[0].equals("director") && words[1].equals("title"))) {
                        List<Film> all = new ArrayList<>(filmStorage.searchByTitle(query));
                        all.addAll(replaceGenresByNull(filmStorage.searchByDirector(query)));
                        return all;
                    }

                    if ((words[0].equals("title") && words[1].equals("director"))) {
                        List<Film> all = new ArrayList<>(filmStorage.searchByDirector(query));
                        all.addAll(replaceGenresByNull(filmStorage.searchByTitle(query)));
                        return all;
                    }

                } else {
                    log.info("Not enough parameters to search for");
                    throw new FilmNotFoundException("Not enough parameters to search for");
                }
            }
            log.info("Not enough parameters to search for");
            throw new FilmNotFoundException("Not enough parameters to search for");
        }
    }

    private List<Film> replaceGenresByNull(List<Film> films) {
        return films.stream().peek(film -> {
                    if (film.getGenres().size() == 0) film.setGenres(null);
                })
                .collect(Collectors.toList());
}
    public List<Film> getCommonFilms(long userId, long friendId) {
        return likesStorage.getCommonFilms(userId,friendId);

    }
}
