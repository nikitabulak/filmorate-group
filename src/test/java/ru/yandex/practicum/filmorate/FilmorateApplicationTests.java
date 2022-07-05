package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikesStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;

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
	private final DirectorStorage directorStorage;
	private final LikesStorage likesStorage;

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

	// directors
	@Test
	public void testCreateNewDirector() {
		Director directorTemplate = new Director(null, "Quentin Tarantino");
		Long directorId = directorStorage.createDirectorAndReturnDirectorWithId(directorTemplate).getId();
		Director searchableDirector = directorStorage.getDirectorByIdFromDb(directorId);
		assertThat(searchableDirector)
				.hasFieldOrPropertyWithValue("id", directorId)
				.hasFieldOrPropertyWithValue("name", "Quentin Tarantino");
		directorStorage.removeDirectorByIdFromStorage(directorId);
	}

	@Test
	public void testUpdateDirector() {
		Director directorTemplate1 = new Director(null, "Quentin Tarantino");
		Long directorId = directorStorage.createDirectorAndReturnDirectorWithId(directorTemplate1).getId();
		Director directorTemplate2 = new Director(directorId, "Quentin Jerome Tarantino");
		directorStorage.updateDirectorInDb(directorTemplate2);
		Director searchableDirector = directorStorage.getDirectorByIdFromDb(directorId);
		assertThat(searchableDirector)
				.hasFieldOrPropertyWithValue("id", directorId)
				.hasFieldOrPropertyWithValue("name", "Quentin Jerome Tarantino");
		directorStorage.removeDirectorByIdFromStorage(directorId);
	}

	@Test
	public void testRemoveDirector() {
		Director directorTemplate1 = new Director(null, "Quentin Tarantino");
		Long directorId = directorStorage.createDirectorAndReturnDirectorWithId(directorTemplate1).getId();
		directorStorage.removeDirectorByIdFromStorage(directorId);
		Throwable thrown = catchThrowable(() -> {
			directorStorage.getDirectorByIdFromDb(directorId);
		});
		assertThat(thrown).isInstanceOf(DirectorNotFoundException.class);
		assertThat(thrown).hasMessageContaining(String.format("Attempt to update director with " +
				"absent id = %d", directorId));
	}

	@Test
	public void testGetAllDirectors() {
		Director directorTemplate1 = new Director(null, "Quentin Tarantino");
		Director directorTemplate2 = new Director(null, "Martin Scorsese");
		Long directorId1 = directorStorage.createDirectorAndReturnDirectorWithId(directorTemplate1).getId();
		Long directorId2 = directorStorage.createDirectorAndReturnDirectorWithId(directorTemplate2).getId();
		List<Director> directors = directorStorage.getAllDirectorsFromDb();
		assertThat(directors.get(0)).hasFieldOrPropertyWithValue("name", "Quentin Tarantino");
		assertThat(directors.get(1)).hasFieldOrPropertyWithValue("name", "Martin Scorsese");
		directorStorage.removeDirectorByIdFromStorage(directorId1);
		directorStorage.removeDirectorByIdFromStorage(directorId2);
	}

	@Test
	public void testCreateAndUpdateDirectorsOfFilm() {
		Director directorTemplate1 = new Director(null, "Quentin Tarantino");
		Director directorTemplate2 = new Director(null, "Martin Scorsese");
		Long directorId1 = directorStorage.createDirectorAndReturnDirectorWithId(directorTemplate1).getId();
		Long directorId2 = directorStorage.createDirectorAndReturnDirectorWithId(directorTemplate2).getId();
		Film filmTemplate1 = new Film(null, "The hunt for the dead", "When all means are good",
				LocalDate.of(2024, 4, 4), 148, Set.of(new Genre(6, null)),
				new Mpa(5, null), Set.of(new Director(directorId1, null)));
		Film searchableFilm1 = filmDbStorage.add(filmTemplate1);
		assertThat(searchableFilm1.getDirectors().size()).isEqualTo(1);
		assertThat(searchableFilm1.getDirectors().contains(new Director(directorId1, "Quentin Tarantino"))).isTrue();
		Film filmTemplate2 = new Film(searchableFilm1.getId(), "The hunt for the dead", "When all means are good",
				LocalDate.of(2024, 4, 4), 148, Set.of(new Genre(6, null)),
				new Mpa(5, null),
				Set.of(new Director(directorId1, null), new Director(directorId2, null)));
		directorStorage.updateDirectorsOfFilm(filmTemplate2);
		Set<Director> directors = directorStorage.getDirectorsByFilmId(searchableFilm1.getId());
		assertThat(directors.size()).isEqualTo(2);
		assertThat(directors.contains(new Director(directorId1, "Quentin Tarantino"))).isTrue();
		assertThat(directors.contains(new Director(directorId2, "Martin Scorsese"))).isTrue();
		directorStorage.removeDirectorByIdFromStorage(directorId1);
		directorStorage.removeDirectorByIdFromStorage(directorId2);
		filmDbStorage.deleteById(searchableFilm1.getId());
	}

	@Test
	public void testReturnSortedFilmsByDirectorId() {
		Director directorTemplate1 = new Director(null, "Quentin Tarantino");
		Long directorId1 = directorStorage.createDirectorAndReturnDirectorWithId(directorTemplate1).getId();
		Film filmTemplate1 = new Film(null, "Film1", "DescriptionFilm1",
				LocalDate.of(2001, 1, 1), 100, Set.of(new Genre(6, null)),
				new Mpa(5, null), Set.of(new Director(directorId1, null)));
		Film filmTemplate2 = new Film(null, "Film2", "DescriptionFilm2",
				LocalDate.of(2002, 2, 2), 100, Set.of(new Genre(6, null)),
				new Mpa(5, null), Set.of(new Director(directorId1, null)));
		Film filmTemplate3 = new Film(null, "Film3", "DescriptionFilm3",
				LocalDate.of(2003, 3, 3), 100, Set.of(new Genre(6, null)),
				new Mpa(5, null), Set.of(new Director(directorId1, null)));
		Long idFilm2 = filmDbStorage.add(filmTemplate2).getId();
		Long idFilm3 = filmDbStorage.add(filmTemplate3).getId();
		Long idFilm1 = filmDbStorage.add(filmTemplate1).getId();
		List<Film> filmsByYear = directorStorage.getAllFilmsByDirectorOnYear(directorId1);
		assertThat(filmsByYear.get(0)).hasFieldOrPropertyWithValue("id", idFilm1);
		assertThat(filmsByYear.get(1)).hasFieldOrPropertyWithValue("id", idFilm2);
		assertThat(filmsByYear.get(2)).hasFieldOrPropertyWithValue("id", idFilm3);
		User userTemplate1 = new User(null, "mail@user1.ru", "user1", "User1",
				LocalDate.of(1946, 8, 20));
		User userTemplate2 = new User(null, "mail@user2.ru", "user2", "User2",
				LocalDate.of(1946, 8, 20));
		User userTemplate3 = new User(null, "mail@user3.ru", "user3", "User3",
				LocalDate.of(1946, 8, 20));
		Long idUser1 = userDbStorage.add(userTemplate1).getId();
		Long idUser2 = userDbStorage.add(userTemplate2).getId();
		Long idUser3 = userDbStorage.add(userTemplate3).getId();
		likesStorage.addLike(idFilm1, idUser1);
		likesStorage.addLike(idFilm2, idUser1);
		likesStorage.addLike(idFilm3, idUser1);
		likesStorage.addLike(idFilm1, idUser2);
		likesStorage.addLike(idFilm2, idUser2);
		likesStorage.addLike(idFilm1, idUser3);  // film1=3likes, film2=2likes, film3=1like
		List<Film> filmByLikes1 = directorStorage.getAllFilmsByDirectorOnLikes(directorId1);
		assertThat(filmByLikes1.get(0)).hasFieldOrPropertyWithValue("id", idFilm1);
		assertThat(filmByLikes1.get(1)).hasFieldOrPropertyWithValue("id", idFilm2);
		assertThat(filmByLikes1.get(2)).hasFieldOrPropertyWithValue("id", idFilm3);
		likesStorage.removeLike(idFilm1, idUser1);
		likesStorage.removeLike(idFilm1, idUser2);
		likesStorage.removeLike(idFilm1, idUser3); // film1=0likes, film2=2likes, film3=1like
		List<Film> filmByLikes2 = directorStorage.getAllFilmsByDirectorOnLikes(directorId1);
		assertThat(filmByLikes2.get(0)).hasFieldOrPropertyWithValue("id", idFilm2);
		assertThat(filmByLikes2.get(1)).hasFieldOrPropertyWithValue("id", idFilm3);
		assertThat(filmByLikes2.get(2)).hasFieldOrPropertyWithValue("id", idFilm1);
		userDbStorage.deleteById(idUser1);
		userDbStorage.deleteById(idUser2);
		userDbStorage.deleteById(idUser3);
		filmDbStorage.deleteById(idFilm1);
		filmDbStorage.deleteById(idFilm2);
		filmDbStorage.deleteById(idFilm3);
		directorStorage.removeDirectorByIdFromStorage(directorId1);
	}
}
