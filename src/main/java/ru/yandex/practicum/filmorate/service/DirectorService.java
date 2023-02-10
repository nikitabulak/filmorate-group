package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorDao;

    public List<Director> getAllDirectors() {
        return directorDao.getAllDirectorsFromDb();
    }

    public Director getDirectorById(Long id) {
        Director director = directorDao.getDirectorByIdFromDb(id);
        log.info("Found director id = {}", director);
        return director;
    }

    public Director createDirector(Director director) {
        return directorDao.createDirectorAndReturnDirectorWithId(director);
    }

    public Director updateDirector(Director director) {
        return directorDao.updateDirectorInDb(director);
    }

    public void removeDirectorById(Long id) {
        directorDao.removeDirectorByIdFromStorage(id);
    }

    public List<Film> getSortedFilmsByDirectorId(Long directorId, Optional<String> param) {
        if (param.isEmpty()) throw new ValidationException("Attempt to get sorted films with " +
                "empty parameter");
        if (!directorDao.isDirectorExists(directorId)) throw new DirectorNotFoundException(
                String.format("Attempt to get sorted films with absent director id = %d", directorId));

        String sortParameter = param.get().trim().toLowerCase();
        switch (sortParameter) {
            case "year": {
                List<Film> films = directorDao.getAllFilmsByDirectorOnYear(directorId);
                filmConvertedGenres(films);
                return films;
            }
            case "likes": {
                List<Film> films = directorDao.getAllFilmsByDirectorOnLikes(directorId);
                filmConvertedGenres(films);
                return films;
            }
            default:
                throw new DirectorNotFoundException(String.format("Attempt to get sorted films with " +
                        "unknown parameter = %s", sortParameter));
        }
    }

    private void filmConvertedGenres(List<Film> films) {
        for (Film film : films) {
            // незнаю, зачем тестах ожидают null, а не empty collection, но сделал
            if (film.getGenres().isEmpty()) film.setGenres(null);
        }
    }
}
