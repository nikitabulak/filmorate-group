package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);
    Film update(Film film);
    Collection<Film> getAll();
    Optional<Film> getById(Long id);
    Film delete(Film film);
    public List<Film> searchByTitle(String query);
    public List<Film> searchByDirector(String query);
}
