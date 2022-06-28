package ru.yandex.practicum.filmorate.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

@Configuration
@ConditionalOnProperty(
        value = "storage.implementation",
        havingValue = "memory"
)

public class InMemoryStorageConfiguration {
    @Bean
    public InMemoryFilmStorage inMemoryFilmStorage () {
        return new InMemoryFilmStorage();
    }

    @Bean
    public InMemoryUserStorage inMemoryUserStorage() {
        return new InMemoryUserStorage();
    }
}
