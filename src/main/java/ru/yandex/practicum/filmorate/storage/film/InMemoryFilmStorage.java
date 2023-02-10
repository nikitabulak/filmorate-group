package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.NotImplementedException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film add(Film film) {
        films.put(film.getId(), film);
        log.info("New film added: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Long id = film.getId();
        if (!films.containsKey(id))
            throw new FilmNotFoundException(String.format("Attempt to update film with absent id = %d", id));
        films.put(id, film);
        log.info("Film {} has been successfully updated", film);
        return film;
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public Optional<Film> getById(Long id) {
        return Optional.of(films.get(id));
    }

    @Override
    public Film delete(Film film) {
        if (films.containsKey(film.getId())) {
            log.info("Film {} was deleted", film);
            return films.remove(film.getId());
        }
        else throw new FilmNotFoundException(String.format("Attempt to delete film with absent id = %d", film.getId()));
    }

    @Override

    public List<Film> searchByTitle(String query) {
        throw new NotImplementedException();
    }

    @Override
    public List<Film> searchByDirector(String query) {
        throw new NotImplementedException();
}
    public void deleteById(Long id){
        throw new NotImplementedException();

    }
}
