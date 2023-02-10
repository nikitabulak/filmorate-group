package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final EventStorage eventStorage;

    public Collection<Review> getAllReviews(Long filmId, Long count) {
        return reviewStorage.getAll(filmId, count);
    }

    public Review getReviewById(final Long id) {
        return reviewStorage.getById(id).orElseThrow(() ->
                new ReviewNotFoundException(String.format("Attempt to get review with absent id = %d",
                        id)));
    }

    public Review addReview(Review review) {
        Review rev = reviewStorage.add(review);
        eventStorage.addNewEvent(new Event.Builder()
                .setCurrentTimestamp()
                .setUserId(rev.getUserId())
                .setEventType(EventType.REVIEW)
                .setOperationType(OperationType.ADD)
                .setEntityId(rev.getReviewId())
                .build());
        return rev;
    }

    public Review updateReview(Review review) {
        if (reviewStorage.isReviewExists(review.getReviewId())) {
            Review rev = reviewStorage.getById(review.getReviewId()).get();
            eventStorage.addNewEvent(new Event.Builder()
                    .setCurrentTimestamp()
                    .setUserId(rev.getUserId())
                    .setEventType(EventType.REVIEW)
                    .setOperationType(OperationType.UPDATE)
                    .setEntityId(rev.getReviewId())
                    .build());
        }
        return reviewStorage.update(review);
    }

    public void deleteReviewById(Long id) {
        Review review = null;
        try {
            review = reviewStorage.getById(id).get();
        } catch (NoSuchElementException e) {
            // ignore
        }
        reviewStorage.deleteById(id);
        eventStorage.addNewEvent(new Event.Builder()
                .setCurrentTimestamp()
                .setUserId(review.getUserId())
                .setEventType(EventType.REVIEW)
                .setOperationType(OperationType.REMOVE)
                .setEntityId(review.getFilmId())
                .build());
    }

    public void addUserLike(Long id, Long userId) {
        reviewStorage.addLike(id, userId);
    }

    public void addUserDislike(Long id, Long userId) {
        reviewStorage.addDislike(id, userId);
    }

    public void deleteUserLike(Long id, Long userId) {
        reviewStorage.deleteLike(id, userId);
    }

    public void deleteUserDislike(Long id, Long userId) {
        reviewStorage.deleteDislike(id, userId);
    }
}
