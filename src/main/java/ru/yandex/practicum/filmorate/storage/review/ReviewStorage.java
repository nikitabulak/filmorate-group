package ru.yandex.practicum.filmorate.storage.review;

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
public class ReviewStorage {

    public static final String GET_ALL_REVIEWS_QUERY_TEMPLATE = "SELECT * FROM REVIEWS ORDER BY useful DESC LIMIT ?";

    public static final String GET_ALL_REVIEWS_BY_FILM_ID_QUERY_TEMPLATE = "SELECT * FROM REVIEWS WHERE film_id = ? " +
            "ORDER BY useful DESC LIMIT ?";

    public static final String GET_REVIEW_BY_ID_QUERY_TEMPLATE = "SELECT * FROM REVIEWS WHERE review_id = ?";

    public static final String UPDATE_REVIEW_BY_ID_QUERY_TEMPLATE = "UPDATE REVIEWS SET content = ?, is_positive = ? " +
            "WHERE review_id = ?";

    public static final String DELETE_REVIEW_BY_ID_QUERY_TEMPLATE = "DELETE FROM REVIEWS WHERE review_id = ?";

    private final JdbcTemplate jdbcTemplate;

    private final UserDbStorage userDbStorage;

    private final FilmDbStorage filmDbStorage;

    public ReviewStorage(JdbcTemplate jdbcTemplate, UserDbStorage userDbStorage, FilmDbStorage filmDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDbStorage = userDbStorage;
        this.filmDbStorage = filmDbStorage;
    }

    public Collection<Review> getAll(Long filmId, Long count) {
        String query;
        Long[] args;
        int[] argTypes;
        if (filmId == null) {
            query = GET_ALL_REVIEWS_QUERY_TEMPLATE;
            args = new Long[]{count};
            argTypes = new int[]{Types.BIGINT};
        } else {
            query = GET_ALL_REVIEWS_BY_FILM_ID_QUERY_TEMPLATE;
            args = new Long[]{filmId, count};
            argTypes = new int[]{Types.BIGINT,Types.BIGINT};
        }
        return jdbcTemplate.query(query, args, argTypes, (rs, rowNum) -> new Review(
                rs.getLong("review_id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getLong("user_id"),
                rs.getLong("film_id"),
                rs.getLong("useful"))
        );
    }

    public Review add(Review review) {
        review.setUseful(0L);
        if (!userDbStorage.isUserExists(review.getUserId())){
            throw new UserNotFoundException(String.format("Attempt to create review by user with absent id = %d", review.getUserId()));
        }
        if (!filmDbStorage.isFilmExists(review.getFilmId())){
            throw new FilmNotFoundException(String.format("Attempt to create review to film with absent id = %d", review.getFilmId()));
        }
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("REVIEWS")
                .usingGeneratedKeyColumns("review_id");
        review.setReviewId(simpleJdbcInsert.executeAndReturnKey(review.toMap()).longValue());
        log.info("New review added: {}", review);
        return review;
    }

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

    public Review update(Review review) {
        if (isReviewExists(review.getReviewId())) {
            jdbcTemplate.update(UPDATE_REVIEW_BY_ID_QUERY_TEMPLATE,
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

    public void deleteById(Long reviewId) {
        if (isReviewExists(reviewId)) {
            jdbcTemplate.update(DELETE_REVIEW_BY_ID_QUERY_TEMPLATE, reviewId);
        } else {
            throw new ReviewNotFoundException(String.format("Attempt to delete review with " +
                    "absent id = %d", reviewId));
        }
    }

    public boolean isReviewExists(Long id) {
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet(GET_REVIEW_BY_ID_QUERY_TEMPLATE, id);
        return reviewRows.first();
    }
}
