package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReviewRatingsDao {

    public static final String ADD_LIKE_QUERY_TEMPLATE = "INSERT INTO REVIEW_RATINGS (review_id, user_id, liked) VALUES (?,?,true)";
    public static final String ADD_DISLIKE_QUERY_TEMPLATE = "INSERT INTO REVIEW_RATINGS (review_id, user_id, liked) VALUES (?,?,false)";
    public static final String DELETE_LIKE_QUERY_TEMPLATE = "DELETE FROM REVIEW_RATINGS WHERE review_id = ? AND user_id = ? AND liked = true";
    public static final String DELETE_DISLIKE_QUERY_TEMPLATE = "DELETE FROM REVIEW_RATINGS WHERE review_id = ? AND user_id = ? AND liked = false";
    public static final String COUNT_USEFUL_QUERY_TEMPLATE = "SELECT (SELECT COUNT(review_id) FROM REVIEW_RATINGS WHERE review_id = ? AND liked = true) - " +
            "(SELECT COUNT(review_id) FROM REVIEW_RATINGS WHERE review_id = ? AND liked = false) as count_useful";
    public static final String UPDATE_USEFUL_IN_REVIEWS_QUERY_TEMPLATE = "UPDATE REVIEWS SET useful = ? " +
            "WHERE review_id = ?";
    private final JdbcTemplate jdbcTemplate;

    public ReviewRatingsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addLike(Long reviewId, Long userId){
        deleteDislike(reviewId,userId);
        jdbcTemplate.update(ADD_LIKE_QUERY_TEMPLATE, reviewId, userId);
        jdbcTemplate.update(UPDATE_USEFUL_IN_REVIEWS_QUERY_TEMPLATE, countUseful(reviewId), reviewId);
    }

    public void addDislike(Long reviewId, Long userId){
        deleteLike(reviewId, userId);
        jdbcTemplate.update(ADD_DISLIKE_QUERY_TEMPLATE, reviewId, userId);
        jdbcTemplate.update(UPDATE_USEFUL_IN_REVIEWS_QUERY_TEMPLATE, countUseful(reviewId), reviewId);
    }

    public void deleteLike(Long reviewId, Long userId){
        jdbcTemplate.update(DELETE_LIKE_QUERY_TEMPLATE, reviewId, userId);
        jdbcTemplate.update(UPDATE_USEFUL_IN_REVIEWS_QUERY_TEMPLATE, countUseful(reviewId), reviewId);
    }

    public void deleteDislike(Long reviewId, Long userId){
        jdbcTemplate.update(DELETE_DISLIKE_QUERY_TEMPLATE, reviewId, userId);
        jdbcTemplate.update(UPDATE_USEFUL_IN_REVIEWS_QUERY_TEMPLATE, countUseful(reviewId), reviewId);
    }

    public Long countUseful(Long reviewId){
        SqlRowSet count = jdbcTemplate.queryForRowSet(COUNT_USEFUL_QUERY_TEMPLATE, reviewId, reviewId);
        count.next();
        return count.getLong("count_useful");
    }
}
