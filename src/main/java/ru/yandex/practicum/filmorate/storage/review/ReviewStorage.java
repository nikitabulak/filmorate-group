package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Collection<Review> getAll(Long filmId, Long count);

    Review add(Review review);

    Optional<Review> getById(Long reviewId);

    Review update(Review review);

    void deleteById(Long reviewId);

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void deleteLike(Long reviewId, Long userId);

    void deleteDislike(Long reviewId, Long userId);

    Long countUseful(Long reviewId);

    boolean isReviewExists(Long id);
}
