package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    @Override
    public List<Director> getAllDirectorsFromDb() {
        String sqlQuery = "SELECT director_id, director_name FROM directors";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> new Director(rs.getLong("director_id"),
                rs.getString("director_name")));
    }

    @Override
    public Director getDirectorByIdFromDb(Long id) {
        String sqlQuery = "SELECT director_name FROM directors WHERE director_id = ?";
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (directorRows.next()) {
            return new Director(id, directorRows.getString("director_name"));
        } else {
            throw new DirectorNotFoundException(String.format("Attempt to update director with " +
                    "absent id = %d", id));
        }
    }

    @Override
    public Director createDirectorAndReturnDirectorWithId(Director director) {
        String sqlQuery = "INSERT INTO directors (director_name) " +
                "values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        director.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("New director has been successfully added: {}", director);
        return director;
    }

    @Override
    public Director updateDirectorInDb(Director director) {
        if (isDirectorExists(director.getId())) {
            String sqlQuery = "UPDATE directors SET director_name = ? WHERE director_id = ?";
            jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
            log.info("Director {} has been successfully updated", director);
            return director;
        } else {
            throw new DirectorNotFoundException(String.format("Attempt to update director with " +
                    "absent id = %d", director.getId()));
        }
    }

    @Override
    public void removeDirectorByIdFromStorage(Long id) {
        if (isDirectorExists(id)) {
            String sql = "DELETE FROM DIRECTORS WHERE director_id = ?";
            jdbcTemplate.update(sql, id);
            log.info("Director  with id = {} has been successfully removed", id);
        } else throw new DirectorNotFoundException(String.format("Attempt to delete director with " +
                "absent id = %d", id));
    }

    @Override
    public void updateDirectorsOfFilm(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_DIRECTOR WHERE film_id = ?", film.getId());
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                if (isDirectorExists(director.getId())) {
                    jdbcTemplate.update("INSERT INTO FILM_DIRECTOR (film_id, director_id) VALUES (?, ?)",
                            film.getId(), director.getId());
                    log.info("Director id = {} has been successfully updated in the film: {}", director.getId(), film);
                } else {
                    throw new DirectorNotFoundException(String.format("Attempt to create film with " +
                            "absent director id = %d", director.getId()));
                }
            }
        }
    }

    @Override
    public Set<Director> getDirectorsByFilmId(Long filmId) {
        String sql = "SELECT DIRECTORS.DIRECTOR_ID, DIRECTOR_NAME FROM FILM_DIRECTOR JOIN DIRECTORS " +
                "ON FILM_DIRECTOR.DIRECTOR_ID = DIRECTORS.DIRECTOR_ID " +
                "WHERE FILM_ID = ?";
        return new TreeSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> new Director(
                        rs.getLong("director_id"),
                        rs.getString("director_name")),
                filmId
        ));
    }

    @Override
    public List<Film> getAllFilmsByDirectorOnLikes(Long directorId) {
        String sql = "SELECT f.film_id, f.name, f.description, f.releasedate, f.duration, f.rate_id " +
                "FROM FILMS AS f " +
                "LEFT JOIN LIKES AS l ON f.film_id = l.film_id " +
                "LEFT JOIN FILM_DIRECTOR AS fd ON f.film_id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "GROUP BY f.FILM_ID " +
                "ORDER BY COUNT(l.FILM_ID) DESC;";
        return collectSortedListOfFilms(sql, directorId);
    }

    @Override
    public List<Film> getAllFilmsByDirectorOnYear(Long directorId) {
        String sql = "SELECT f.film_id, f.name, f.description, f.releasedate, f.duration, f.rate_id " +
                "FROM FILMS AS f " +
                "LEFT JOIN FILM_DIRECTOR AS fd ON f.film_id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "ORDER BY f.releaseDate;";
        return collectSortedListOfFilms(sql, directorId);
    }

    private List<Film> collectSortedListOfFilms(String sql, Long directorId) {
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                rs.getLong("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("releaseDate").toLocalDate(),
                rs.getInt("duration"),
                genreStorage.getFilmGenres(rs.getLong("film_id")),
                mpaStorage.getMpa(rs.getInt("rate_id")),
                getDirectorsByFilmId(rs.getLong("film_id"))
        ), directorId);
    }

    @Override
    public boolean isDirectorExists(Long id) {
        String sql = "SELECT director_name FROM directors WHERE director_id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, id);
        return userRows.next();
    }
}
