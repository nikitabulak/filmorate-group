package ru.yandex.practicum.filmorate.storage.like;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LikesStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    public LikesStorage(JdbcTemplate jdbcTemplate,
                        GenreStorage genreStorage,
                        MpaStorage mpaStorage,
                        FilmDbStorage filmDbStorage, UserDbStorage userDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.filmDbStorage = filmDbStorage;
        this.userDbStorage = userDbStorage;
    }

    public void addLike(Long id, Long userId) {
        if (!filmDbStorage.isFilmExists(id)) throw new FilmNotFoundException("Film not found");
        if (!userDbStorage.isUserExists(userId)) throw new UserNotFoundException("User not found");
        String sql = "INSERT INTO LIKES (user_id, film_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, id);
        log.info("User id = {} add like to film id = {}", userId, id);
    }

    public void removeLike(Long id, Long userId) {
        if (!filmDbStorage.isFilmExists(id)) throw new FilmNotFoundException("Film not found");
        if (!userDbStorage.isUserExists(userId)) throw new UserNotFoundException("User not found");
        if (!isLikeExist(userId, id)) throw new UserNotFoundException("User didn't add like to film");
        String sql = "DELETE FROM LIKES WHERE user_id = ? AND film_id = ?";
        jdbcTemplate.update(sql, userId, id);
    }

    private boolean isLikeExist(Long userId, Long filmId) {
        String sql = "SELECT * FROM LIKES WHERE user_id = ? AND film_id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, userId, filmId);
        return userRows.next();
    }

    public List<Film> getPopular(int count, int genreId, int year) {
        String sql = "";
        List<Film> films = new ArrayList<>();
        if (genreId == -1 && year == -1) {
            log.info("Filtering populars films no parameters");
            sql = "SELECT FILMS.FILM_ID, NAME, DESCRIPTION, RELEASEDATE, DURATION, RATE_ID , " +
                    "COUNT(L.USER_ID) as RATING FROM FILMS " +
                    "LEFT JOIN LIKES L on FILMS.FILM_ID = L.FILM_ID " +
                    "GROUP BY FILMS.FILM_ID " +
                    "ORDER BY RATING DESC LIMIT ?";
            films = jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                    rs.getLong("film_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("releaseDate").toLocalDate(),
                    rs.getInt("duration"),
                    genreStorage.getFilmGenres(rs.getLong("film_id")),
                    mpaStorage.getMpa(rs.getInt("rate_id")),
                    rs.getLong("rating")
            ), count);

        }
        if (genreId > 0 && year == -1) {
            log.info("Filtering populars films by genre");
            sql = "SELECT FILMS.FILM_ID, NAME, DESCRIPTION, RELEASEDATE, DURATION, RATE_ID , " +
                    "COUNT(L.USER_ID) as RATING FROM FILMS " +
                    "LEFT JOIN LIKES L on FILMS.FILM_ID = L.FILM_ID " +
                    "LEFT JOIN FILM_GENRES F on FILMS.FILM_ID = F.FILM_ID " +
                    "WHERE F.GENRE_ID=?" +
                    " GROUP BY FILMS.FILM_ID,  F.GENRE_ID " +
                    "ORDER BY RATING DESC LIMIT ?";
            films = jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                    rs.getLong("film_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("releaseDate").toLocalDate(),
                    rs.getInt("duration"),
                    genreStorage.getFilmGenres(rs.getLong("film_id")),
                    mpaStorage.getMpa(rs.getInt("rate_id")),
                    rs.getLong("rating")
            ), genreId, count);
        }
        if (genreId == -1 && year > 0) {
            log.info("Filtering populars films by year");
            sql = "SELECT FILMS.FILM_ID, NAME, DESCRIPTION, RELEASEDATE, DURATION, RATE_ID , " +
                    "COUNT(L.USER_ID) as RATING FROM FILMS " +
                    "LEFT JOIN LIKES L on FILMS.FILM_ID = L.FILM_ID " +
                    "WHERE EXTRACT(YEAR FROM RELEASEDATE)=?" +
                    " GROUP BY FILMS.FILM_ID" +
                    " ORDER BY RATING DESC LIMIT ?";
            films = jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                    rs.getLong("film_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("releaseDate").toLocalDate(),
                    rs.getInt("duration"),
                    genreStorage.getFilmGenres(rs.getLong("film_id")),
                    mpaStorage.getMpa(rs.getInt("rate_id")),
                    rs.getLong("rating")
            ), year, count);
        }
        if (genreId > 0 && year > 0) {
            log.info("Filtering populars films by genre and year");
            sql = "SELECT FILMS.FILM_ID, NAME, DESCRIPTION, RELEASEDATE, DURATION, RATE_ID , " +
                    "COUNT(L.USER_ID) as RATING FROM FILMS " +
                    "LEFT JOIN LIKES L on FILMS.FILM_ID = L.FILM_ID " +
                    "LEFT JOIN FILM_GENRES F on FILMS.FILM_ID = F.FILM_ID " +
                    "WHERE F.GENRE_ID=?" +
                    " AND EXTRACT(YEAR FROM RELEASEDATE)=?" +
                    " GROUP BY FILMS.FILM_ID,  F.GENRE_ID " +
                    "ORDER BY RATING DESC LIMIT ?";
            films = jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                    rs.getLong("film_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("releaseDate").toLocalDate(),
                    rs.getInt("duration"),
                    genreStorage.getFilmGenres(rs.getLong("film_id")),
                    mpaStorage.getMpa(rs.getInt("rate_id")),
                    rs.getLong("rating")
            ), genreId, year, count);
        }
        if (genreId < -1 && year < -1) {
            throw new ValidationException(String.format("Incorrect parameters for filtering populars - films" +
                    " genreid = %d and year = %d.", genreId, year));
        }

        return films;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        if (!userDbStorage.isUserExists(userId)) throw new UserNotFoundException("User not found");
        if (!userDbStorage.isUserExists(friendId)) throw new UserNotFoundException("User not found");
        String sql = "SELECT *" +
                "FROM films AS f " +
                "LEFT JOIN (SELECT film_id, COUNT(film_id) AS count_like FROM likes GROUP BY film_id) USING (film_id) " +
                "RIGHT JOIN likes AS l1 ON f.film_id = l1.film_id " +
                "RIGHT JOIN likes AS l2 ON l1.film_id = l2.film_id " +
                "WHERE l1.user_id = ? AND l2.user_id = ? " +
                "ORDER BY count_like DESC;";

        return getFilmsBySql(sql, userId, friendId);
    }

    public List<Film> getRecommendations(Long userId) {
        if (!userDbStorage.isUserExists(userId)) throw new UserNotFoundException("User not found");
        List<Film> films = new ArrayList<>();

        String sql = "SELECT l2.user_Id " +
                "FROM likes AS l1 " +
                "JOIN likes AS l2 ON l1.film_id = l2.film_id " +
                "WHERE l1.user_id = ? AND l1.user_id<>l2.user_id " +
                "GROUP BY l2.user_id " +
                "ORDER BY COUNT(l2.user_id) DESC " +
                "LIMIT 1";

        List<Long> id = jdbcTemplate.queryForList(sql, Long.class, userId);

        if (id.isEmpty()) {
            return films;
        }

        sql = "SELECT * " +
                "FROM films AS f " +
                "JOIN (SELECT film_id FROM likes " +
                "WHERE USER_ID = ? " +
                "EXCEPT " +
                "SELECT film_id FROM likes " +
                "WHERE USER_ID = ?) AS l ON l.film_id = f.film_id";

        return getFilmsBySql(sql, id.get(0), userId);
    }

    private List<Film> getFilmsBySql(String sql, Long userId, Long id) {
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                rs.getLong("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("releaseDate").toLocalDate(),
                rs.getInt("duration"),
                genreStorage.getFilmGenres(rs.getLong("film_id")),
                mpaStorage.getMpa(rs.getInt("rate_id"))
        ), userId, id);
        for (Film f : films) {
            if (f.getGenres().isEmpty()) {
                f.setGenres(null);
            }
        }
        return films;
    }
}
