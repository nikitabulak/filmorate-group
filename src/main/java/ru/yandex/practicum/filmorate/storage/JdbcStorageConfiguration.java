package ru.yandex.practicum.filmorate.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmJdbcMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

@Configuration
@ConditionalOnProperty(
        value = "storage.implementation",
        havingValue = "jdbc",
        matchIfMissing = true
)

public class JdbcStorageConfiguration {
    @Bean
    public FilmDbStorage filmDbStorage(JdbcTemplate jdbcTemplate,
                                       GenreStorage genreStorage,
                                       FilmJdbcMapper filmJdbcMapper) {
        return new FilmDbStorage(jdbcTemplate, genreStorage, filmJdbcMapper);
    }

    @Bean
    public UserDbStorage userDbStorage(JdbcTemplate jdbcTemplate) {
        return new UserDbStorage(jdbcTemplate);
    }
}
