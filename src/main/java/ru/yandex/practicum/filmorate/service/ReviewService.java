package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewRatingsDao;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final ReviewRatingsDao reviewRatingsDao;
    private final EventStorage eventStorage;

    public ReviewService(ReviewStorage reviewStorage,
                         ReviewRatingsDao reviewRatingsDao,
                         EventStorage eventStorage){
        this.reviewStorage = reviewStorage;
        this.reviewRatingsDao = reviewRatingsDao;
        this.eventStorage = eventStorage;
    }

    public Collection<Review> getAllReviews(Long filmId, Long count){
        return reviewStorage.getAll(filmId, count);
    }

    public Review getReviewById(final Long id){
        return reviewStorage.getById(id).orElseThrow(() ->
                new ReviewNotFoundException(String.format("Attempt to get review with absent id = %d",
                       id)));
    }

    public Review addReview(Review review){
        Review rev = reviewStorage.add(review);
        eventStorage.addNewEvent(new Event.Builder()
                .setCurrentTimestamp()
                .setUserId(rev.getUserId())
                .setEventType(EventType.REVIEW)
                .setOperationType(OperationType.ADD)
                .setEntityId(rev.getId())
                .build());
        return rev;
    }

    public Review updateReview(Review review){
        if (reviewStorage.isReviewExists(review.getId())) {
            Review rev = reviewStorage.getById(review.getId()).get();
            eventStorage.addNewEvent(new Event.Builder()
                    .setCurrentTimestamp()
                    .setUserId(rev.getUserId())
                    .setEventType(EventType.REVIEW)
                    .setOperationType(OperationType.UPDATE)
                    .setEntityId(rev.getId())
                    .build());
        }
        return reviewStorage.update(review);
    }

    public void deleteReviewById(Long id){
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
                .setEntityId(id)
                .build());
    }

    public void addUserLike(Long id, Long userId){
        reviewRatingsDao.addLike(id, userId);
    }

    public void addUserDislike(Long id, Long userId){
        reviewRatingsDao.addDislike(id, userId);
    }

    public void deleteUserLike(Long id, Long userId){
        reviewRatingsDao.deleteLike(id, userId);
    }

    public void deleteUserDislike(Long id, Long userId){
        reviewRatingsDao.deleteDislike(id, userId);
    }
}
