package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
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

	@Test
	public void testFindUserById() {
		User user = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		Long id = userDbStorage.addUser(user).getId();
		Optional<User> userOptional = userDbStorage.getUserById(id);

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
		Long id = userDbStorage.addUser(user).getId();
		userUpdate.setId(id);
		userDbStorage.updateUser(userUpdate);
		Optional<User> userOptional = userDbStorage.getUserById(id);

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(u -> assertThat(u)
						.hasFieldOrPropertyWithValue("login", "NewDolore"));
	}

	@Test
	public void testUserDelete() {
		User user = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.of(1946, 8, 20));
		userDbStorage.addUser(user);
		userDbStorage.deleteUser(user);
		Optional<User> userOptional = userDbStorage.getUserById(1L);
		assertThat(userOptional).isEmpty();
	}

	@Test
	public void testFindFilmById() {
		Film film = new Film(null, "name", "description",
				LocalDate.of(1975, 5, 17),
				100);
		film.setMpa(new Mpa(1, null));
		Long id = filmDbStorage.addFilm(film).getId();
		Optional<Film> filmOptional = filmDbStorage.getFilmById(id);

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(f ->
						assertThat(f).hasFieldOrPropertyWithValue("id", id)
				);
	}

	@Test
	public void testFilmUpdate() {
		Film film = new Film(null, "name", "description",
				LocalDate.of(1975, 5, 17),
				100);
		film.setMpa(new Mpa(1, null));
		Film filmUpdate = new Film(null, "NewName", "description",
				LocalDate.of(1975, 5, 17),
				100);
		filmUpdate.setMpa(new Mpa(1, null));
		Long id = filmDbStorage.addFilm(film).getId();
		filmUpdate.setId(id);
		filmDbStorage.updateFilm(filmUpdate);
		Optional<Film> filmOptional = filmDbStorage.getFilmById(id);

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
		filmDbStorage.addFilm(film);
		filmDbStorage.deleteFilm(film);
		Optional<Film> filmOptional = filmDbStorage.getFilmById(1L);
		assertThat(filmOptional).isEmpty();
	}

}
