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
import ru.yandex.practicum.filmorate.storage.review.ReviewRatingsDao;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)

public class ReviewRatingsDaoTest {
    private final ReviewRatingsDao reviewRatingsDao;
    private final JdbcTemplate jdbcTemplate;

    @BeforeAll
    public void insertUserAndFilmAndReview() {
        String query = "INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES ('testUser1@mail.ru', 'testUserLogin', 'testUser', '1994-06-15'), " +
                "('testUser2@mail.ru', 'testUserLogin', 'testUser', '1994-06-15')";
        jdbcTemplate.update(query);
        query = "INSERT INTO FILMS (NAME, DESCRIPTION, RELEASEDATE, DURATION, RATE_ID) values ( 'testFilmName'," +
                "'testFilmDescription','1984-05-23','120',1)";
        jdbcTemplate.update(query);
        query = "INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL) values ('testContent', true, 1, 1, 0)";
        jdbcTemplate.update(query);
    }

    @AfterEach
    public void clearTable() {
        String query = "delete from REVIEW_RATINGS";
        jdbcTemplate.update(query);
        query = "update REVIEWS set useful = 0 where review_id = 1";
        jdbcTemplate.update(query);
    }

    @Test
    public void testAddLike() {
        reviewRatingsDao.addLike(1L, 1L);

        String query = "select REVIEW_ID, USER_ID, LIKED from REVIEW_RATINGS";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);
        sqlRowSet.next();
        assertThat(sqlRowSet.getLong("review_id")).isEqualTo(1);
        assertThat(sqlRowSet.getLong("user_id")).isEqualTo(1);
        assertThat(sqlRowSet.getBoolean("liked")).isEqualTo(true);

        query = "select USEFUL from REVIEWS where REVIEW_ID = 1";
        sqlRowSet = jdbcTemplate.queryForRowSet(query);
        sqlRowSet.next();
        assertThat(sqlRowSet.getLong("useful")).isEqualTo(1);
    }

    @Test
    public void testAddDislike() {
        reviewRatingsDao.addDislike(1L, 1L);

        String query = "select REVIEW_ID, USER_ID, LIKED from REVIEW_RATINGS";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);
        sqlRowSet.next();
        assertThat(sqlRowSet.getLong("review_id")).isEqualTo(1);
        assertThat(sqlRowSet.getLong("user_id")).isEqualTo(1);
        assertThat(sqlRowSet.getBoolean("liked")).isEqualTo(false);

        query = "select USEFUL from REVIEWS where REVIEW_ID = 1";
        sqlRowSet = jdbcTemplate.queryForRowSet(query);
        sqlRowSet.next();
        assertThat(sqlRowSet.getLong("useful")).isEqualTo(-1);
    }

    @Test
    public void testDeleteLike() {
        String query = "INSERT INTO REVIEW_RATINGS (REVIEW_ID, USER_ID, LIKED) values (1, 1, true)";
        jdbcTemplate.update(query);
        query = "update REVIEWS set useful = 1 where review_id = 1";
        jdbcTemplate.update(query);

        reviewRatingsDao.deleteLike(1L, 1L);

        query = "select REVIEW_ID, USER_ID, LIKED from REVIEW_RATINGS";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);
        assertThat(sqlRowSet.next()).isEqualTo(false);

        query = "select USEFUL from REVIEWS where REVIEW_ID = 1";
        sqlRowSet = jdbcTemplate.queryForRowSet(query);
        sqlRowSet.next();
        assertThat(sqlRowSet.getLong("useful")).isEqualTo(0);
    }

    @Test
    public void testDeleteDislike() {
        String query = "INSERT INTO REVIEW_RATINGS (REVIEW_ID, USER_ID, LIKED) values (1, 1, false)";
        jdbcTemplate.update(query);
        query = "update REVIEWS set useful = -1 where review_id = 1";
        jdbcTemplate.update(query);

        reviewRatingsDao.deleteDislike(1L, 1L);

        query = "select REVIEW_ID, USER_ID, LIKED from REVIEW_RATINGS";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);
        assertThat(sqlRowSet.next()).isEqualTo(false);

        query = "select USEFUL from REVIEWS where REVIEW_ID = 1";
        sqlRowSet = jdbcTemplate.queryForRowSet(query);
        sqlRowSet.next();
        assertThat(sqlRowSet.getLong("useful")).isEqualTo(0);
    }
}
