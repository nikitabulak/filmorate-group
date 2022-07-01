package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewRatingsDao;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

@Service
@Slf4j
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final ReviewRatingsDao reviewRatingsDao;

    public ReviewService(ReviewStorage reviewStorage, ReviewRatingsDao reviewRatingsDao){
        this.reviewStorage = reviewStorage;
        this.reviewRatingsDao = reviewRatingsDao;
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
        return reviewStorage.add(review);
    }

    public Review updateReview(Review review){
        return reviewStorage.update(review);
    }

    public void deleteReviewById(Long id){
        reviewStorage.deleteById(id);
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
