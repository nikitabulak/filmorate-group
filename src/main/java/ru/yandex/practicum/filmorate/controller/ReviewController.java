package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
//@Validated
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {

    private ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public Collection<Review> findAll(@RequestParam(required = false) Long filmId,
                                      @RequestParam(defaultValue = "10", required = false) Long count) {
        log.info("Request to get reviews, filmId = {}, count = {}", filmId == null ? "all" : filmId, count);
        return reviewService.getAllReviews(filmId, count);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        log.info("Request to get review by id = {}", id);
        return reviewService.getReviewById(id);
    }

    @PostMapping
    public Review createReview(@Valid @RequestBody Review review) {
        log.info("Request to add review {}", review);
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Request to update review {}", review);
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReviewById(@PathVariable Long id) {
        log.info("Request to delete review by id = {}", id);
        reviewService.deleteReviewById(id);
    }
    //------------------------------------------------------
    @PutMapping("/{id}/like/{userId}")
    public void addUserLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Request to add like to review with id = {} by user with userId = {}", id, userId);
        reviewService.addUserLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addUserDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Request to add dislike to review with id = {} by user with userId = {}", id, userId);
        reviewService.addUserDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteUserLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Request to delete like from review with id = {} by user with userId = {}", id, userId);
        reviewService.deleteUserLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteUserDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Request to delete dislike from review with id = {} by user with userId = {}", id, userId);
        reviewService.deleteUserDislike(id, userId);
    }
}
