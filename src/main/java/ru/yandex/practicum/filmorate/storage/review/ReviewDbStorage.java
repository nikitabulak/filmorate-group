package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.sql.Types;
import java.util.Collection;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    public static final String GET_REVIEW_BY_ID_QUERY_TEMPLATE = "SELECT * FROM REVIEWS WHERE review_id = ?";
    public static final String UPDATE_USEFUL_IN_REVIEWS_QUERY_TEMPLATE = "UPDATE REVIEWS SET useful = ? " +
            "WHERE review_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    @Override
    public Collection<Review> getAll(Long filmId, Long count) {
        String getAllReviewsQuery;
        Long[] args;
        int[] argTypes;
        if (filmId == null) {
            getAllReviewsQuery = "SELECT * FROM REVIEWS ORDER BY useful DESC LIMIT ?";
            args = new Long[]{count};
            argTypes = new int[]{Types.BIGINT};
        } else {
            getAllReviewsQuery = "SELECT * FROM REVIEWS WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
            args = new Long[]{filmId, count};
            argTypes = new int[]{Types.BIGINT, Types.BIGINT};
        }
        return jdbcTemplate.query(getAllReviewsQuery, args, argTypes, (rs, rowNum) -> new Review(
                rs.getLong("review_id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getLong("user_id"),
                rs.getLong("film_id"),
                rs.getLong("useful"))
        );
    }

    @Override
    public Review add(Review review) {
        review.setUseful(0L);
        if (!userDbStorage.isUserExists(review.getUserId())) {
            throw new UserNotFoundException(String.format("Attempt to create review by user with absent id = %d", review.getUserId()));
        }
        if (!filmDbStorage.isFilmExists(review.getFilmId())) {
            throw new FilmNotFoundException(String.format("Attempt to create review to film with absent id = %d", review.getFilmId()));
        }
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("REVIEWS")
                .usingGeneratedKeyColumns("review_id");
        review.setReviewId(simpleJdbcInsert.executeAndReturnKey(review.toMap()).longValue());
        log.info("New review added: {}", review);
        return review;
    }

    @Override
    public Optional<Review> getById(Long reviewId) {
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet(GET_REVIEW_BY_ID_QUERY_TEMPLATE, reviewId);
        if (reviewRows.first()) {
            Review review = new Review(
                    reviewRows.getLong("review_id"),
                    reviewRows.getString("content"),
                    reviewRows.getBoolean("is_positive"),
                    reviewRows.getLong("user_id"),
                    reviewRows.getLong("film_id"),
                    reviewRows.getLong("useful"));
            log.info("Found review with id = {}", reviewId);
            return Optional.of(review);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Review update(Review review) {
        if (isReviewExists(review.getReviewId())) {
            String updateReviewByIdQuery = "UPDATE REVIEWS SET content = ?, is_positive = ? WHERE review_id = ?";
            jdbcTemplate.update(updateReviewByIdQuery,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getReviewId());
            log.info("Review {} has been succesfully updated", review);
            return review;
        } else {
            throw new ReviewNotFoundException(String.format("Attempt to update review with " +
                    "absent id = %d", review.getReviewId()));
        }
    }

    @Override
    public void deleteById(Long reviewId) {
        if (isReviewExists(reviewId)) {
            String deleteReviewByIdQuery = "DELETE FROM REVIEWS WHERE review_id = ?";
            jdbcTemplate.update(deleteReviewByIdQuery, reviewId);
        } else {
            throw new ReviewNotFoundException(String.format("Attempt to delete review with " +
                    "absent id = %d", reviewId));
        }
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        deleteDislike(reviewId, userId);
        String addLikeQuery = "INSERT INTO REVIEW_RATINGS (review_id, user_id, liked) VALUES (?,?,true)";
        jdbcTemplate.update(addLikeQuery, reviewId, userId);
        jdbcTemplate.update(UPDATE_USEFUL_IN_REVIEWS_QUERY_TEMPLATE, countUseful(reviewId), reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        deleteLike(reviewId, userId);
        String addDislikeQuery = "INSERT INTO REVIEW_RATINGS (review_id, user_id, liked) VALUES (?,?,false)";
        jdbcTemplate.update(addDislikeQuery, reviewId, userId);
        jdbcTemplate.update(UPDATE_USEFUL_IN_REVIEWS_QUERY_TEMPLATE, countUseful(reviewId), reviewId);
    }

    @Override
    public void deleteLike(Long reviewId, Long userId) {
        String deleteLikeQuery = "DELETE FROM REVIEW_RATINGS WHERE review_id = ? AND user_id = ? AND liked = true";
        jdbcTemplate.update(deleteLikeQuery, reviewId, userId);
        jdbcTemplate.update(UPDATE_USEFUL_IN_REVIEWS_QUERY_TEMPLATE, countUseful(reviewId), reviewId);
    }

    @Override
    public void deleteDislike(Long reviewId, Long userId) {
        String deleteDislikeQuery = "DELETE FROM REVIEW_RATINGS WHERE review_id = ? AND user_id = ? AND liked = false";
        jdbcTemplate.update(deleteDislikeQuery, reviewId, userId);
        jdbcTemplate.update(UPDATE_USEFUL_IN_REVIEWS_QUERY_TEMPLATE, countUseful(reviewId), reviewId);
    }

    @Override
    public Long countUseful(Long reviewId) {
        String countUsefulQuery = "SELECT (SELECT COUNT(review_id) FROM REVIEW_RATINGS WHERE review_id = ? AND liked = true) - " +
                "(SELECT COUNT(review_id) FROM REVIEW_RATINGS WHERE review_id = ? AND liked = false) as count_useful";
        SqlRowSet count = jdbcTemplate.queryForRowSet(countUsefulQuery, reviewId, reviewId);
        count.next();
        return count.getLong("count_useful");
    }

    @Override
    public boolean isReviewExists(Long id) {
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet(GET_REVIEW_BY_ID_QUERY_TEMPLATE, id);
        return reviewRows.first();
    }
}
