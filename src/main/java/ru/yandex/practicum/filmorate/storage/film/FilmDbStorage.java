package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private GenreStorage genreStorage;
    private final FilmJdbcMapper filmJdbcMapper;
    private static final String UPDATE_FILM = "UPDATE FILMS SET " +
            "name = ?, description = ?, releaseDate = ?, duration = ?, " +
            "rate_id = ? WHERE film_id = ?";
    private static final String GET_ALL_FILMS = "SELECT * FROM FILMS ";
    private static final String GET_FILM_BY_ID = "SELECT * FROM FILMS WHERE film_id = ?";
    private static final String DELETE_FILM_BY_ID = "DELETE FROM FILMS WHERE film_id = ?";
    private static final String UPDATE_FILM_MPA= "UPDATE FILMS SET RATE_ID = ? WHERE FILM_ID = ?";

    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         GenreStorage genreStorage,
                         FilmJdbcMapper filmJdbcMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.filmJdbcMapper = filmJdbcMapper;
    }

    @Override
    public Film add(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("film_id");
        film.setId(simpleJdbcInsert.executeAndReturnKey(film.toMap()).longValue());
        if (film.getGenres() != null) genreStorage.addGenresOfFilm(film);
        jdbcTemplate.update(UPDATE_FILM_MPA,
                    film.getMpa().getId(),
                    film.getId()
            );

        log.info("New film added: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (isFilmExists(film.getId())) {
            jdbcTemplate.update(UPDATE_FILM,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId());
            genreStorage.deleteGenresOfFilm(film);
            genreStorage.addGenresOfFilm(film);
            log.info("Film {} has been successfully updated", film);
            return film;
        } else {
            throw new FilmNotFoundException(String.format("Attempt to update film with " +
                    "absent id = %d", film.getId()));
        }
    }

    @Override
    public Collection<Film> getAll() {
        return jdbcTemplate.query(GET_ALL_FILMS, filmJdbcMapper);
    }

    @Override
    public Optional<Film> getById(Long id) {
        try {
            Film film = jdbcTemplate.queryForObject(GET_FILM_BY_ID, filmJdbcMapper, id);
            if (film.getGenres().size() == 0) film.setGenres(null);
            log.info("Found film id = {}", id);
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            log.info("Film id = {} is absent", id);
            return Optional.empty();
        }
    }

    @Override
    public Film delete(Film film) {
        if (isFilmExists(film.getId())) {
            jdbcTemplate.update(DELETE_FILM_BY_ID, film.getId());
            if (film.getGenres() != null) genreStorage.deleteGenresOfFilm(film);
            log.info("film id = {} is deleted", film.getId());
            return film;
        } else throw new FilmNotFoundException(String.format("Attempt to delete film with " +
                "absent id = %d", film.getId()));
    }

    public boolean isFilmExists(Long id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(GET_FILM_BY_ID, id);
        return userRows.next();
    }
}