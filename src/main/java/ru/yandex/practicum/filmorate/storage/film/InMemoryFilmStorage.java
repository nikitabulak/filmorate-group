package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        films.put(film.getId(), film);
        log.info("New film added: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        Long id = film.getId();
        if (!films.containsKey(id))
            throw new FilmNotFoundException(String.format("Attempt to update film with absent id = %d", id));
        films.put(id, film);
        log.info("Film {} has been successfully updated", film);
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film getFilmById(Long id) {
        if (!films.containsKey(id))
            throw new FilmNotFoundException(String.format("Request film by id when id is absent, id = %d", id));
        return films.get(id);
    }

    @Override
    public Film deleteFilm(Film film) {
        if (films.containsKey(film.getId())) {
            log.info("Film {} was deleted", film);
            return films.remove(film.getId());
        }
        else throw new FilmNotFoundException(String.format("Attempt to delete film with absent id = %d", film.getId()));
    }
}
