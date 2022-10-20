package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AdditionalMethodsOfControllerMockMvcTest {
    private final MockMvc mockMvc;
    private final ObjectMapper mapper;

    Director directorTemplate1 = new Director(null, "Quentin Tarantino");

    @Test
    public void shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/directors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void shouldNotReturnDirettorById() throws Exception {
        mockMvc.perform(get("/directors/{id}", 10)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateDirector() throws Exception {
        String jacksonDirector = mapper.writeValueAsString(directorTemplate1);
        mockMvc.perform(post("/directors").content(jacksonDirector).
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Quentin Tarantino"));
        mockMvc.perform(get("/directors/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Quentin Tarantino"));
    }

    @Test
    public void shouldNotUpdateNonExistingDirector() throws Exception {
        Director director = new Director(1L, "Quentin Tarantino");
        String jacksonDirector = mapper.writeValueAsString(director);
        System.out.println(jacksonDirector);
        mockMvc.perform(put("/directors").content(jacksonDirector).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldUpdateDirector() throws Exception {
        String jacksonDirector = mapper.writeValueAsString(directorTemplate1);
        Director updateDirector = new Director(1L, "Quentin Jerome Tarantino");
        String jacksonUpdateDirector = mapper.writeValueAsString(updateDirector);
        mockMvc.perform(post("/directors").content(jacksonDirector).
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(put("/directors").content(jacksonUpdateDirector)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Quentin Jerome Tarantino"));
    }

    @Test
    public void shouldReturnAllDirectors() throws Exception {
        String jacksonDirector1 = mapper.writeValueAsString(directorTemplate1);
        Director directorTemplate2 = new Director(null, "Martin Scorsese");
        String jacksonDirector2 = mapper.writeValueAsString(directorTemplate2);
        Director directorTemplate3 = new Director(null, "Никита Михалков");
        String jacksonDirector3 = mapper.writeValueAsString(directorTemplate3);
        mockMvc.perform(post("/directors").content(jacksonDirector1).
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(post("/directors").content(jacksonDirector2).
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(post("/directors").content(jacksonDirector3).
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/directors").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Quentin Tarantino"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].name").value("Martin Scorsese"))
                .andExpect(jsonPath("$[2].id").value("3"))
                .andExpect(jsonPath("$[2].name").value("Никита Михалков"));
    }

    @Test
    public void shouldRemoveDirector() throws Exception {
        String jacksonDirector1 = mapper.writeValueAsString(directorTemplate1);
        Director directorTemplate2 = new Director(null, "Martin Scorsese");
        String jacksonDirector2 = mapper.writeValueAsString(directorTemplate2);
        mockMvc.perform(post("/directors").content(jacksonDirector1).
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(post("/directors").content(jacksonDirector2).
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/directors/{id}", 1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/directors").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("2"))
                .andExpect(jsonPath("$[0].name").value("Martin Scorsese"));
    }

    @Test
    public void shouldReturnSortedFilmsByDirectorId() throws Exception {
        mockMvc.perform(post("/directors").content(mapper.writeValueAsString(directorTemplate1))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        Film filmTemplate1 = new Film(null, "Film1", "DescriptionFilm1",        // id=3
                LocalDate.of(2001, 1, 1), 100, Set.of(new Genre(6, null)),
                new Mpa(5, null), Set.of(new Director(1L, null)));
        Film filmTemplate2 = new Film(null, "Film2", "DescriptionFilm2",        // id=1
                LocalDate.of(2002, 2, 2), 100, Set.of(new Genre(6, null)),
                new Mpa(5, null), Set.of(new Director(1L, null)));
        Film filmTemplate3 = new Film(null, "Film3", "DescriptionFilm3",        // id=2
                LocalDate.of(2003, 3, 3), 100, Set.of(new Genre(6, null)),
                new Mpa(5, null), Set.of(new Director(1L, null)));
        mockMvc.perform(post("/films").content(mapper.writeValueAsString(filmTemplate2))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(post("/films").content(mapper.writeValueAsString(filmTemplate3))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(post("/films").content(mapper.writeValueAsString(filmTemplate1))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(get("/films/director/{directorId}?sortBy=year", 1)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Film1"))
                .andExpect(jsonPath("$[1].name").value("Film2"))
                .andExpect(jsonPath("$[2].name").value("Film3"));
        User userTemplate1 = new User(null, "mail@user1.ru", "user1", "User1",
                LocalDate.of(1946, 8, 20));
        User userTemplate2 = new User(null, "mail@user2.ru", "user2", "User2",
                LocalDate.of(1946, 8, 20));
        User userTemplate3 = new User(null, "mail@user3.ru", "user3", "User3",
                LocalDate.of(1946, 8, 20));
        mockMvc.perform(post("/users").content(mapper.writeValueAsString(userTemplate1))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(post("/users").content(mapper.writeValueAsString(userTemplate2))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(post("/users").content(mapper.writeValueAsString(userTemplate3))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", 3, 1)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", 1, 1)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", 2, 1)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", 3, 2)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", 1, 2)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", 3, 3)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        // film1=3likes, film2=2likes, film3=1like
        mockMvc.perform(get("/films/director/{directorId}?sortBy=likes", 1)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Film1"))
                .andExpect(jsonPath("$[1].name").value("Film2"))
                .andExpect(jsonPath("$[2].name").value("Film3"));
        mockMvc.perform(delete("/films/{id}/like/{userId}", 3, 1)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(delete("/films/{id}/like/{userId}", 3, 2)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mockMvc.perform(delete("/films/{id}/like/{userId}", 3, 3)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        // film1=0likes, film2=2likes, film3=1like
        mockMvc.perform(get("/films/director/{directorId}?sortBy=likes", 1)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Film2"))
                .andExpect(jsonPath("$[1].name").value("Film3"))
                .andExpect(jsonPath("$[2].name").value("Film1"));
    }
}
