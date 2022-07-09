package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface DirectorStorage {
    List<Director> getAllDirectorsFromDb();

    Director getDirectorByIdFromDb(Long id);

    Director createDirectorAndReturnDirectorWithId(Director director);

    Director updateDirectorInDb(Director director);

    void removeDirectorByIdFromStorage(Long id);

    void updateDirectorsOfFilm(Film film);

    Set<Director> getDirectorsByFilmId(Long filmId);

    List<Film> getAllFilmsByDirectorOnLikes(Long directorId);

    List<Film> getAllFilmsByDirectorOnYear(Long directorId);

    boolean isDirectorExists(Long id);
}
