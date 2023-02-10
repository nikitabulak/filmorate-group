package ru.yandex.practicum.filmorate.storage.like;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface LikesStorage {
    void addLike(Long id, Long userId);

    void removeLike(Long id, Long userId);

    List<Film> getPopular(int count, int genreId, int year);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getRecommendations(Long userId);
}
