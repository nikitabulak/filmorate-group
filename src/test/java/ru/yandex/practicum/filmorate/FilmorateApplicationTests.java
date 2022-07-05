package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {

	private final UserDbStorage userDbStorage;
	private final FilmDbStorage filmDbStorage;
	private final EventStorage eventStorage;
	private final UserService userService;
	private final FilmService filmService;
	private final ReviewService reviewService;

	@Test
	public void testFindUserById() {
		User user = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		Long id = userDbStorage.add(user).getId();
		Optional<User> userOptional = userDbStorage.getById(id);

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(u ->
						assertThat(u).hasFieldOrPropertyWithValue("id", id)
				);
	}

	@Test
	public void testUserUpdate() {
		User user = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		User userUpdate = new User(null, "mail@mail.ru", "NewDolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		Long id = userDbStorage.add(user).getId();
		userUpdate.setId(id);
		userDbStorage.update(userUpdate);
		Optional<User> userOptional = userDbStorage.getById(id);

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(u -> assertThat(u)
						.hasFieldOrPropertyWithValue("login", "NewDolore"));
	}

	@Test
	public void testUserDelete() {
		User user = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		userDbStorage.add(user);
		Long id = user.getId();
		userDbStorage.deleteById(id);
		Optional<User> userOptional = userDbStorage.getById(id);
		assertThat(userOptional).isEmpty();
	}

	@Test
	public void testFindFilmById() {
		Film film = new Film(null, "name", "description",
				LocalDate.of(1975, 5, 17),
				100);
		film.setMpa(new Mpa(1, null));
		Long id = filmDbStorage.add(film).getId();
		Optional<Film> filmOptional = filmDbStorage.getById(id);

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(f ->
						assertThat(f).hasFieldOrPropertyWithValue("id", id)
				);
	}

	@Test
	public void testUpdate() {
		Film film = new Film(null, "name", "description",
				LocalDate.of(1975, 5, 17),
				100);
		film.setMpa(new Mpa(1, null));
		Film filmUpdate = new Film(null, "NewName", "description",
				LocalDate.of(1975, 5, 17),
				100);
		filmUpdate.setMpa(new Mpa(1, null));
		Long id = filmDbStorage.add(film).getId();
		filmUpdate.setId(id);
		filmDbStorage.update(filmUpdate);
		Optional<Film> filmOptional = filmDbStorage.getById(id);

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(u -> assertThat(u)
						.hasFieldOrPropertyWithValue("name", "NewName"));
	}

	@Test
	public void testFilmDelete() {
		Film film = new Film(null, "name", "description",
				LocalDate.of(1975, 5, 17),
				100);
		film.setMpa(new Mpa(1, null));
		Long id = filmDbStorage.add(film).getId();
		filmDbStorage.deleteById(id);
		Optional<Film> filmOptional = filmDbStorage.getById(id);
		assertThat(filmOptional).isEmpty();
	}

	@Test
	public void testAddEventWhenUserAddFriend() {
		User userOne = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		User userTwo = new User(null, "mail@yandex.ru", "qwerty", "Nick Name",
				LocalDate.of(1948, 7, 25));
		Long idOne = userDbStorage.add(userOne).getId();
		Long idTwo = userDbStorage.add(userTwo).getId();
		userService.addFriend(idOne, idTwo);
		Event event = eventStorage.getEventsByUserId(idOne).get(0);

		assertThat(event).hasFieldOrPropertyWithValue("userId", idOne)
				.hasFieldOrPropertyWithValue("eventType", EventType.FRIEND)
				.hasFieldOrPropertyWithValue("operation", OperationType.ADD)
				.hasFieldOrPropertyWithValue("entityId", idTwo);
	}

	@Test
	public void testAddEventWhenUserDeleteFriend() {
		User userOne = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		User userTwo = new User(null, "mail@yandex.ru", "qwerty", "Nick Name",
				LocalDate.of(1948, 7, 25));
		Long idOne = userDbStorage.add(userOne).getId();
		Long idTwo = userDbStorage.add(userTwo).getId();
		userService.addFriend(idOne, idTwo);
		userService.deleteFriend(idOne, idTwo);
		Event event = eventStorage.getEventsByUserId(idOne).get(1);

		assertThat(event).hasFieldOrPropertyWithValue("userId", idOne)
				.hasFieldOrPropertyWithValue("eventType", EventType.FRIEND)
				.hasFieldOrPropertyWithValue("operation", OperationType.REMOVE)
				.hasFieldOrPropertyWithValue("entityId", idTwo);
	}

	@Test
	public void testAddEventWhenUserAddLike() {
		User user = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		Film film = new Film(null, "name", "description",
				LocalDate.of(1975, 5, 17),
				100);
		film.setMpa(new Mpa(1, null));
		Long id = userDbStorage.add(user).getId();
		Long filmId = filmDbStorage.add(film).getId();
		filmService.addLike(filmId, id);
		Event event = eventStorage.getEventsByUserId(id).get(0);

		assertThat(event).hasFieldOrPropertyWithValue("userId", id)
				.hasFieldOrPropertyWithValue("eventType", EventType.LIKE)
				.hasFieldOrPropertyWithValue("operation", OperationType.ADD)
				.hasFieldOrPropertyWithValue("entityId", filmId);
	}

	@Test
	public void testAddEventWhenUserRemoveLike() {
		User user = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		Film film = new Film(null, "name", "description",
				LocalDate.of(1975, 5, 17),
				100);
		film.setMpa(new Mpa(1, null));
		Long id = userDbStorage.add(user).getId();
		Long filmId = filmDbStorage.add(film).getId();
		filmService.addLike(filmId, id);
		filmService.removeLike(filmId, id);
		Event event = eventStorage.getEventsByUserId(id).get(1);

		assertThat(event).hasFieldOrPropertyWithValue("userId", id)
				.hasFieldOrPropertyWithValue("eventType", EventType.LIKE)
				.hasFieldOrPropertyWithValue("operation", OperationType.REMOVE)
				.hasFieldOrPropertyWithValue("entityId", filmId);
	}

	@Test
	public void testAddEventWhenUserAddReview() {
		User user = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		Film film = new Film(null, "name", "description",
				LocalDate.of(1975, 5, 17),
				100);
		film.setMpa(new Mpa(1, null));
		Long id = userDbStorage.add(user).getId();
		Long filmId = filmDbStorage.add(film).getId();
		Review review = new Review(null, "content", true, id, filmId, 0L);
		reviewService.addReview(review);

		Event event = eventStorage.getEventsByUserId(id).get(0);

		assertThat(event).hasFieldOrPropertyWithValue("userId", id)
				.hasFieldOrPropertyWithValue("eventType", EventType.REVIEW)
				.hasFieldOrPropertyWithValue("operation", OperationType.ADD)
				.hasFieldOrPropertyWithValue("entityId", review.getReviewId());
	}

	@Test
	public void testAddEventWhenUserUpdateReview() {
		User user = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		Film film = new Film(null, "name", "description",
				LocalDate.of(1975, 5, 17),
				100);
		film.setMpa(new Mpa(1, null));
		Long id = userDbStorage.add(user).getId();
		Long filmId = filmDbStorage.add(film).getId();
		Review review = new Review(null, "content", true, id, filmId, 0L);
		reviewService.addReview(review);
		Review reviewUpdate = new Review(review.getReviewId(), "contentUpdate", true, id, filmId, 0L);
		reviewService.updateReview(reviewUpdate);

		Event event = eventStorage.getEventsByUserId(id).get(1);

		assertThat(event).hasFieldOrPropertyWithValue("userId", id)
				.hasFieldOrPropertyWithValue("eventType", EventType.REVIEW)
				.hasFieldOrPropertyWithValue("operation", OperationType.UPDATE)
				.hasFieldOrPropertyWithValue("entityId", review.getReviewId());
	}

	@Test
	public void testAddEventWhenUserDeleteReview() {
		User user = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		Film film = new Film(null, "name", "description",
				LocalDate.of(1975, 5, 17),
				100);
		film.setMpa(new Mpa(1, null));
		Long id = userDbStorage.add(user).getId();
		Long filmId = filmDbStorage.add(film).getId();
		Review review = new Review(null, "content", true, id, filmId, 0L);
		reviewService.addReview(review);
		reviewService.deleteReviewById(review.getReviewId());

		Event event = eventStorage.getEventsByUserId(id).get(1);

		assertThat(event).hasFieldOrPropertyWithValue("userId", id)
				.hasFieldOrPropertyWithValue("eventType", EventType.REVIEW)
				.hasFieldOrPropertyWithValue("operation", OperationType.REMOVE)
				.hasFieldOrPropertyWithValue("entityId", filmId);
	}
}
