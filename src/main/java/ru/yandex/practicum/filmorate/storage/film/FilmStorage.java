package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film addFilm (Film film);
    Film updateFilm (Film film);
    Collection<Film> getFilms();
    Film getFilmById(Long id);
    Film deleteFilm(Film film);
}
