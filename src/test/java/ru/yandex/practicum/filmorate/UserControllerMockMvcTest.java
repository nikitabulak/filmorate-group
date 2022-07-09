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
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class UserControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserController controller;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearTables() {
        jdbcTemplate.update("DELETE FROM FILMS");
        jdbcTemplate.update("DELETE FROM USERS");
        jdbcTemplate.update("ALTER TABLE FILMS ALTER COLUMN FILM_ID RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE USERS ALTER COLUMN USER_ID RESTART WITH 1");
    }

    @Test
    void shouldAddUser() throws Exception {
        mockMvc.perform(
                        post("/users")
                                .content("{\n" +
                                        "  \"login\": \"dolore\",\n" +
                                        "  \"name\": \"est adipisicing\",\n" +
                                        "  \"email\": \"mail@mail.ru\",\n" +
                                        "  \"birthday\": \"1946-08-20\"\n" +
                                        "}" +
                                        "}")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"id\":1," +
                        "\"email\":\"mail@mail.ru\"," +
                        "\"login\":\"dolore\"," +
                        "\"name\":\"est adipisicing\"," +
                        "\"birthday\":\"1946-08-20\",\"friends\":[]}")));
        ;
    }

    @Test
    void shouldNotAddUserWhenWrongEmailFormat() throws Exception {
        mockMvc.perform(
                post("/users")
                        .content("{\n" +
                                "  \"login\": \"dolore\",\n" +
                                "  \"name\": \"est adipisicing\",\n" +
                                "  \"email\": \"mailmail.ru\",\n" +
                                "  \"birthday\": \"1946-08-20\"\n" +
                                "}" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddUserWhenLoginContainsWhitespace() throws Exception {
        mockMvc.perform(
                post("/users")
                        .content("{\n" +
                                "  \"login\": \"dolore qwerty\",\n" +
                                "  \"name\": \"est adipisicing\",\n" +
                                "  \"email\": \"mail@mail.ru\",\n" +
                                "  \"birthday\": \"1946-08-20\"\n" +
                                "}" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddUserWhenLoginIsEmpty() throws Exception {
        mockMvc.perform(
                post("/users")
                        .content("{\n" +
                                "  \"login\": \"\",\n" +
                                "  \"name\": \"est adipisicing\",\n" +
                                "  \"email\": \"mail@mail.ru\",\n" +
                                "  \"birthday\": \"1946-08-20\"\n" +
                                "}" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldAddUserWhenNameIsEmpty() throws Exception {
        mockMvc.perform(
                        post("/users")
                                .content("{\n" +
                                        "  \"login\": \"dolore\",\n" +
                                        "  \"name\": \"\",\n" +
                                        "  \"email\": \"mail@mail.ru\",\n" +
                                        "  \"birthday\": \"1946-08-20\"\n" +
                                        "}" +
                                        "}")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().string(containsString("\"name\":\"dolore\"")));
    }

    @Test
    void shouldNotAddUserWhenBirthdayIsInTheFuture() throws Exception {
        mockMvc.perform(
                post("/users")
                        .content("{\n" +
                                "  \"login\": \"dolore\",\n" +
                                "  \"name\": \"est adipisicing\",\n" +
                                "  \"email\": \"mail@mail.ru\",\n" +
                                "  \"birthday\": \"2946-08-20\"\n" +
                                "}" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void testFeed() throws Exception {
        User userOne = new User(null, "mail@mail.ru", "dolore", "Nick Name",
                LocalDate.of(1946, 8, 20));
        User userTwo = new User(null, "mail@yandex.ru", "qwerty", "Nick Name",
                LocalDate.of(1948, 7, 25));
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(userOne))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(userTwo))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(
                put("/users/1/friends/2")
        ).andExpect(status().isOk());
        mockMvc.perform(
                get("/users/1/feed")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].eventType").value("FRIEND"))
                .andExpect(jsonPath("$[0].operation").value("ADD"))
                .andExpect(jsonPath("$[0].entityId").value("2"));
    }

    @Test
    void testCommonFilms() throws Exception {
        String sqlQuery = "DELETE FROM films";
        jdbcTemplate.update(sqlQuery);
        sqlQuery = "ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1";
        jdbcTemplate.update(sqlQuery);
        sqlQuery = "DELETE FROM likes";
        jdbcTemplate.update(sqlQuery);
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
        Film film3 = new Film(3L,
                "New film2",
                "Film description2",
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
                post("/films")
                        .content(objectMapper.writeValueAsString(film3))
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
        mockMvc.perform(get("/users/1/recommendations"))
                .andExpect(status().isOk());
        assertTrue(controller.getRecommendations(1L).isEmpty());
        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/1/like/2"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/2/like/2"))
                .andExpect(status().isOk());
        assertEquals(1, controller.getRecommendations(1L).size());
        assertEquals("New film", new ArrayList<>(controller.getRecommendations(1L)).get(0).getName());
        mockMvc.perform(put("/films/3/like/2"))
                .andExpect(status().isOk());
        assertEquals(2, controller.getRecommendations(1L).size());
        assertEquals("New film", new ArrayList<>(controller.getRecommendations(1L)).get(0).getName());
        assertEquals("New film2", new ArrayList<>(controller.getRecommendations(1L)).get(1).getName());
    }
}
