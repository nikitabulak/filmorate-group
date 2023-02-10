package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.NotImplementedException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.*;

@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private GenreStorage genreStorage;
    private MpaStorage mpaStorage;
    private final DirectorStorage directorDao;

    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         GenreStorage genreStorage,
                         MpaStorage mpaStorage, DirectorStorage directorDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.directorDao = directorDao;
    }

    @Override
    public Film add(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("film_id");
        film.setId(simpleJdbcInsert.executeAndReturnKey(film.toMap()).longValue());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update("INSERT INTO FILM_GENRES (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId());
            }
        }

        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) { // если режиссер есть - добавляет их в FILM_DIRECTOR
                if (directorDao.isDirectorExists(director.getId())) {  //проверка присутствия режиссера в DIRECTORS
                    jdbcTemplate.update("INSERT INTO FILM_DIRECTOR (film_id, director_id) VALUES (?, ?)",
                            film.getId(), director.getId());
                } else {
                    throw new DirectorNotFoundException(String.format("Attempt to create film with " +
                            "absent director id = %d", director.getId()));
                }
            }
        }
        film.setDirectors(directorDao.getDirectorsByFilmId(film.getId()));
        jdbcTemplate.update("UPDATE FILMS SET RATE_ID = ? WHERE FILM_ID = ?",
                film.getMpa().getId(),
                film.getId()
        );
        log.info("New film added: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (isFilmExists(film.getId())) {
            String sqlQuery = "UPDATE FILMS SET " +
                    "name = ?, description = ?, releaseDate = ?, duration = ?, " +
                    "rate_id = ? WHERE film_id = ?";
            jdbcTemplate.update(sqlQuery,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId());
            genreStorage.updateGenresOfFilm(film);
            directorDao.updateDirectorsOfFilm(film);
            log.info("Film {} has been successfully updated", film);
            return film;
        } else {
            throw new FilmNotFoundException(String.format("Attempt to update film with " +
                    "absent id = %d", film.getId()));
        }
    }

    @Override
    public Collection<Film> getAll() {
        String sql = "SELECT * FROM FILMS ";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                rs.getLong("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("releaseDate").toLocalDate(),
                rs.getInt("duration"),
                genreStorage.getFilmGenres(rs.getLong("film_id")),
                mpaStorage.getMpa(rs.getInt("rate_id")),
                directorDao.getDirectorsByFilmId(rs.getLong("film_id"))
        ));
    }

    @Override
    public Optional<Film> getById(Long id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS WHERE film_id = ?", id);
        if (userRows.next()) {
            Film film = new Film(
                    userRows.getLong("film_id"),
                    userRows.getString("name"),
                    userRows.getString("description"),
                    userRows.getDate("releaseDate").toLocalDate(),
                    userRows.getInt("duration")
            );
            Integer rateMpa = userRows.getInt("rate_id");
            film.setMpa(mpaStorage.getMpa(rateMpa));
            Set<Genre> genres = genreStorage.getFilmGenres(id);
            if (genres.size() != 0) {
                film.setGenres(genreStorage.getFilmGenres(id));
            }
            film.setDirectors(directorDao.getDirectorsByFilmId(id));
            log.info("Found film id = {}", film);
            return Optional.of(film);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(Long filmId){
        if (isFilmExists(filmId)) {
            String sql = "DELETE FROM FILMS WHERE film_id = ?";
            jdbcTemplate.update(sql, filmId);
        } else throw new FilmNotFoundException(String.format("Attempt to delete film with " +
                "absent id = %d", filmId));
    }

    public boolean isFilmExists(Long id) {
        String sql = "SELECT * FROM FILMS WHERE film_id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, id);
        return userRows.next();
    }

    @Override

    public List<Film> searchByTitle(String query) {
        String str = "%" + query + "%";
        String sql = "SELECT * FROM FILMS WHERE LOWER(NAME) LIKE LOWER(?)";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                rs.getLong("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("releaseDate").toLocalDate(),
                rs.getInt("duration"),
                genreStorage.getFilmGenres(rs.getLong("film_id")),
                mpaStorage.getMpa(rs.getInt("rate_id")),
                directorDao.getDirectorsByFilmId(rs.getLong("film_id"))
        ), str);
    }

    @Override
    public List<Film> searchByDirector(String query) {
        String str = "%" + query + "%";
        String sql = "select * from FILMS\n" +
                "         left join FILM_DIRECTOR FD on FILMS.FILM_ID = FD.FILM_ID\n" +
                "         left join DIRECTORS D on D.DIRECTOR_ID = FD.DIRECTOR_ID\n" +
                "where lower(d.DIRECTOR_NAME) like lower(?);";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                rs.getLong("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("releaseDate").toLocalDate(),
                rs.getInt("duration"),
                genreStorage.getFilmGenres(rs.getLong("film_id")),
                mpaStorage.getMpa(rs.getInt("rate_id")),
                directorDao.getDirectorsByFilmId(rs.getLong("film_id"))
        ), str);
}
    public Film delete(Film film) {
        deleteById(film.getId());
        return film;

    }
}
