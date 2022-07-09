package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class FilmControllerMockMvcTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FilmController controller;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDB() {
        String sqlQuery = "DELETE FROM films";
        jdbcTemplate.update(sqlQuery);
        sqlQuery = "ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1";
        jdbcTemplate.update(sqlQuery);
        sqlQuery = "DELETE FROM likes";
        jdbcTemplate.update(sqlQuery);
    }

    @Test
    void shouldAddFilm() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"labore nulla\",\n" +
                                "  \"releaseDate\": \"1979-04-17\",\n" +
                                "  \"description\": \"Duis in consequat esse\",\n" +
                                "  \"duration\": 100,\n" +
                                "  \"rate\": 4,\n" +
                                "  \"mpa\": { \"id\": 1}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        assertEquals("labore nulla", controller.getFilm(1L).getName());
        assertEquals("Duis in consequat esse", controller.getFilm(1L).getDescription());
        assertEquals(1, controller.getFilm(1L).getMpa().getId());
    }

    @Test
    void shouldNotAddFilmWithEmptyName() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"\",\n" +
                                "  \"releaseDate\": \"1979-04-17\",\n" +
                                "  \"description\": \"Duis in consequat esse\",\n" +
                                "  \"duration\": 100,\n" +
                                "  \"rate\": 4,\n" +
                                "  \"mpa\": { \"id\": 1}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddFilmWithTooLongDescription() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"labore nulla\",\n" +
                                "  \"releaseDate\": \"1979-04-17\",\n" +
                                "  \"description\": \"qqqqqqqqqqwwwwwwwwwweeeeeeeeeerrrrrrrrrrttttttttttyyyyyyyyyy" +
                                "qqqqqqqqqqqwwwwwwwwwweeeeeeeeeerrrrrrrrrrttttttttttyyyyyyyyyyqqqqqqqqqqwwwwwwwwww" +
                                "wwwwwwwwwweeeeeeeeeerrrrrrrrrrttttttttttyyyyyyyyyyqqqqqqqqqqqqqqqqqqqqqqqqqq\",\n" +
                                "  \"duration\": 100,\n" +
                                "  \"rate\": 4,\n" +
                                "  \"mpa\": { \"id\": 1}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddFilmWithTooOldDate() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"labore nulla\",\n" +
                                "  \"releaseDate\": \"1400-03-25\",\n" +
                                "  \"description\": \"Duis in consequat esse\",\n" +
                                "  \"duration\": 100,\n" +
                                "  \"rate\": 4,\n" +
                                "  \"mpa\": { \"id\": 1}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddFilmWithNegativeDuration() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"labore nulla\",\n" +
                                "  \"releaseDate\": \"1979-04-17\",\n" +
                                "  \"description\": \"Duis in consequat esse\",\n" +
                                "  \"duration\": -100,\n" +
                                "  \"rate\": 4,\n" +
                                "  \"mpa\": { \"id\": 1}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void testCommonFilms() throws Exception {
        Film film1 = new Film(1L,
                "Missi",
                "Great film",
                LocalDate.of(2000, 2, 22),
                200,
                null,
                new Mpa(1, "G"));
        Film film2 = new Film(2L,
                "New film",
                "Film description",
                LocalDate.of(2021, 2, 22),
                200,
                null,
                new Mpa(1, "G"));
        User user1 = new User(1L,
                "mail@mail.ru",
                "Tommi",
                "Tom",
                LocalDate.of(1975, 6, 2));
        User user2 = new User(2L,
                "mail@yandex.ru",
                "Angel",
                "Angelina",
                LocalDate.of(1975, 6, 4));
        User user3 = new User(3L,
                "ma@yandex.ru",
                "Devil",
                "DD",
                LocalDate.of(1975, 6, 4));
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film2))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user3))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(get("/films/common?userId=1&friendId=2"))
                .andExpect(status().isOk());
        assertTrue(controller.commonFilms(1L, 2L).isEmpty());
        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/1/like/2"))
                .andExpect(status().isOk());
        assertEquals(1, controller.commonFilms(1L, 2L).size());
        mockMvc.perform(put("/films/2/like/2"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/2/like/1"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/2/like/3"))
                .andExpect(status().isOk());
        assertEquals(2, controller.commonFilms(1L, 2L).size());
        assertEquals("New film", new ArrayList<>(controller.commonFilms(1L, 2L)).get(0).getName());
    }

    @Test
    void testMostPopularsFilms() throws Exception {
        mockMvc.perform(get("/films/popular")).andExpect(status().is2xxSuccessful());
        assertTrue(controller.popularFilms(10, -1, -1).isEmpty());
        Genre genre = new Genre(1, "Комедия");
        Genre genre1 = new Genre(2, "Драма");
        Set<Genre> genres = new TreeSet<>();
        Set<Genre> genres1 = new TreeSet<>();
        genres.add(genre);
        genres1.add(genre1);
        Film film1 = new Film(1L,
                "Missi",
                "Great film",
                LocalDate.of(2000, 2, 22),
                200,
                genres,
                new Mpa(1, "G"));
        Film film2 = new Film(2L,
                "New film",
                "Film description",
                LocalDate.of(2021, 2, 22),
                200,
                genres1,
                new Mpa(1, "G"));
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film2))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(get("/films/popular?genreId=1")).andExpect(status().is2xxSuccessful());
        List<Film> filmsPopular = (List<Film>) controller.popularFilms(10, 1, -1);
        assertEquals("Missi", filmsPopular.get(0).getName());
        mockMvc.perform(get("/films/popular?genreId=2")).andExpect(status().is2xxSuccessful());
        filmsPopular = (List<Film>) controller.popularFilms(10, 2, -1);
        assertEquals("New film", filmsPopular.get(0).getName());
        mockMvc.perform(get("/films/popular?year=2021")).andExpect(status().is2xxSuccessful());
        filmsPopular = (List<Film>) controller.popularFilms(10, -1, 2021);
        assertEquals("New film", filmsPopular.get(0).getName());
        mockMvc.perform(get("/films/popular?year=2000")).andExpect(status().is2xxSuccessful());
        filmsPopular = (List<Film>) controller.popularFilms(10, -1, 2000);
        assertEquals("Missi", filmsPopular.get(0).getName());
        mockMvc.perform(get("/films/popular?genreId=2&year=2021")).andExpect(status().is2xxSuccessful());
        filmsPopular = (List<Film>) controller.popularFilms(10, 2, 2021);
        assertEquals("New film", filmsPopular.get(0).getName());
    }


    @Test
    void testSearch() throws Exception {

        Genre genre = new Genre(1, "Комедия");
        Genre genre1 = new Genre(2, "Драма");
        Director director = new Director(1L, "Director");
        Director director1 = new Director(2L, "Updated");
        Set<Genre> genres = new TreeSet<>();
        Set<Genre> genres1 = new TreeSet<>();
        Set<Director> directors = new TreeSet<>();
        Set<Director> directors1 = new TreeSet<>();
        genres.add(genre);
        genres1.add(genre1);
        directors.add(director);
        directors1.add(director1);
        Film film1 = new Film(1L,
                "Missi",
                "Great film",
                LocalDate.of(2000, 2, 22),
                200,
                genres,
                new Mpa(1, "G"),
                directors);
        Film film2 = new Film(2L,
                "New film",
                "Film description",
                LocalDate.of(2021, 2, 22),
                200,
                genres1,
                new Mpa(1, "G"),
                directors1);


        mockMvc.perform(
                post("/directors")
                        .content(objectMapper.writeValueAsString(director))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(
                post("/directors")
                        .content(objectMapper.writeValueAsString(director1))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film2))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(get("/films/search?query=mis&by=title")).andExpect(status().is2xxSuccessful());
        List<Film> filmsSearch = controller.search("mis", "title");
        assertEquals("Missi", filmsSearch.get(0).getName());
        mockMvc.perform(get("/films/search?query=fil&by=title")).andExpect(status().is2xxSuccessful());
        filmsSearch = controller.search("fil", "title");
        assertEquals("New film", filmsSearch.get(0).getName());
        mockMvc.perform(get("/films/search?query=ire&by=director")).andExpect(status().is2xxSuccessful());
        filmsSearch = controller.search("ire", "director");
        assertEquals("Missi", filmsSearch.get(0).getName());
        mockMvc.perform(get("/films/search?query=pda&by=director")).andExpect(status().is2xxSuccessful());
        filmsSearch = controller.search("pda", "director");
        assertEquals("New film", filmsSearch.get(0).getName());
        mockMvc.perform(get("/films/search?query=ate&by=director,title")).andExpect(status().is2xxSuccessful());
        filmsSearch = controller.search("ate", "director,title");
        assertEquals("New film", filmsSearch.get(0).getName());
        mockMvc.perform(get("/films/search")).andExpect(status().is2xxSuccessful());
        filmsSearch = controller.search(null, null);
        assertEquals(2, filmsSearch.size());
    }


}
