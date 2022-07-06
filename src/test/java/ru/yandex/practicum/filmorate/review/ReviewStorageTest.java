package ru.yandex.practicum.filmorate.review;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)

public class ReviewStorageTest {

    private final ReviewStorage reviewStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeAll
    public void insertUserAndFilm() {
        String query = "INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES ('testUser@mail.ru', 'testUserLogin', 'testUser', '1994-06-15')";
        jdbcTemplate.update(query);
        query = "INSERT INTO FILMS (NAME, DESCRIPTION, RELEASEDATE, DURATION, RATE_ID) values ( 'testFilmName'," +
                "'testFilmDescription','1984-05-23','120',1)";
        jdbcTemplate.update(query);
    }

    @AfterEach
    public void clearTable() {
        String query = "delete from REVIEWS";
        jdbcTemplate.update(query);
        query = "alter table REVIEWS alter column REVIEW_ID restart with 1";
        jdbcTemplate.update(query);
    }

    @Test
    public void testGetAllReviews() {
        String query = "INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL) values ('testContent1', true, 1, 1, 0), " +
                "('testContent2', false, 1, 1, 0)";
        jdbcTemplate.update(query);

        Review review1 = new Review(1L, "testContent1", true, 1L, 1L, 0L);
        Review review2 = new Review(2L, "testContent2", false, 1L, 1L, 0L);

        assertThat(reviewStorage.getAll(1L, 10L)).isEqualTo(List.of(review1, review2));
    }

    @Test
    public void testGetReviewById() {
        String query = "INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL) values ('testContent', true, 1, 1, 0)";
        jdbcTemplate.update(query);

        Review review = new Review(1L, "testContent", true, 1L, 1L, 0L);

        assertThat(reviewStorage.getById(1L).get()).isEqualTo(review);
    }

    @Test
    public void testAddReview() {
        Review review = new Review(1L, "testContent", true, 1L, 1L, 0L);
        reviewStorage.add(review);

        String query = "select REVIEW_ID, CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL from REVIEWS";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);
        sqlRowSet.next();
        assertThat(sqlRowSet.getLong("review_id")).isEqualTo(review.getReviewId());
        assertThat(sqlRowSet.getString("content").trim()).isEqualTo(review.getContent());
        assertThat(sqlRowSet.getBoolean("is_positive")).isEqualTo(review.getIsPositive());
        assertThat(sqlRowSet.getLong("user_id")).isEqualTo(review.getUserId());
        assertThat(sqlRowSet.getLong("film_id")).isEqualTo(review.getFilmId());
        assertThat(sqlRowSet.getLong("useful")).isEqualTo(review.getUseful());
    }

    @Test
    public void testUpdateReview() {
        String query = "INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL) values ('testContent', true, 1, 1, 0)";
        jdbcTemplate.update(query);

        Review reviewUpdated = new Review(1L, "testContentUpdated", false, 2L, 2L, 5L);
        reviewStorage.update(reviewUpdated);

        query = "select REVIEW_ID, CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL from REVIEWS";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);
        sqlRowSet.next();
        assertThat(sqlRowSet.getLong("review_id")).isEqualTo(reviewUpdated.getReviewId());
        assertThat(sqlRowSet.getString("content").trim()).isEqualTo(reviewUpdated.getContent());
        assertThat(sqlRowSet.getBoolean("is_positive")).isEqualTo(reviewUpdated.getIsPositive());
        assertThat(sqlRowSet.getLong("user_id")).isEqualTo(1L);
        assertThat(sqlRowSet.getLong("film_id")).isEqualTo(1L);
        assertThat(sqlRowSet.getLong("useful")).isEqualTo(0L);
    }

    @Test
    public void testDeleteReview() {
        String query = "INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL) values ('testContent', true, 1, 1, 0)";
        jdbcTemplate.update(query);

        reviewStorage.deleteById(1L);
        query = "select REVIEW_ID, CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL from REVIEWS";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);
        assertThat(sqlRowSet.next()).isEqualTo(false);
    }
}

