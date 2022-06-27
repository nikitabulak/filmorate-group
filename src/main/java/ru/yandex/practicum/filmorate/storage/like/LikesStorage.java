package ru.yandex.practicum.filmorate.storage.like;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

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

    public List<Film> getPopular(int count) {
       String sql = "SELECT FILMS.FILM_ID, NAME, DESCRIPTION, RELEASEDATE, DURATION, RATE_ID , " +
               "COUNT(L.USER_ID) as RATING FROM FILMS LEFT JOIN LIKES L on FILMS.FILM_ID = L.FILM_ID " +
               "GROUP BY FILMS.FILM_ID " +
               "ORDER BY RATING DESC LIMIT ?";
        System.out.println(count);
        List <Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                rs.getLong("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("releaseDate").toLocalDate(),
                rs.getInt("duration"),
                genreStorage.getFilmGenres(rs.getLong("film_id")),
                mpaStorage.getMpa(rs.getInt("rate_id")),
                rs.getLong("rating")
        ), count);
        return films;
    }
}
