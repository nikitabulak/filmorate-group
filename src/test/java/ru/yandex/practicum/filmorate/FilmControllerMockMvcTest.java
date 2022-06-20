package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Disabled
public class FilmControllerMockMvcTest {
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Test
    void shouldAddFilm() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"nisi eiusmod\",\n" +
                                "  \"description\": \"adipisicing\",\n" +
                                "  \"releaseDate\": \"1967-03-25\",\n" +
                                "  \"duration\": 100\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andExpect(content()
                .string(containsString("{\"id\":1," +
                        "\"name\":\"nisi eiusmod\"," +
                        "\"description\":\"adipisicing\"," +
                        "\"releaseDate\":\"1967-03-25\"," +
                        "\"duration\":\"PT1M40S\"}")));
    }

    @Test
    void shouldNotAddFilmWithEmptyName() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"\",\n" +
                                "  \"description\": \"adipisicing\",\n" +
                                "  \"releaseDate\": \"1967-03-25\",\n" +
                                "  \"duration\": 100\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddFilmWithTooLongDescription() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"nisi eiusmod\",\n" +
                                "  \"description\": \"qqqqqqqqqqwwwwwwwwwweeeeeeeeeerrrrrrrrrrttttttttttyyyyyyyyyy" +
                                "qqqqqqqqqqqwwwwwwwwwweeeeeeeeeerrrrrrrrrrttttttttttyyyyyyyyyyqqqqqqqqqqwwwwwwwwww" +
                                "wwwwwwwwwweeeeeeeeeerrrrrrrrrrttttttttttyyyyyyyyyyqqqqqqqqqqqqqqqqqqqqqqqqqq\",\n" +
                                "  \"releaseDate\": \"1967-03-25\",\n" +
                                "  \"duration\": 100\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddFilmWithTooOldDate() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"nisi eiusmod\",\n" +
                                "  \"description\": \"adipisicing\",\n" +
                                "  \"releaseDate\": \"1400-03-25\",\n" +
                                "  \"duration\": 100\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddFilmWithNegativeDuration() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"nisi eiusmod\",\n" +
                                "  \"description\": \"adipisicing\",\n" +
                                "  \"releaseDate\": \"1967-03-25\",\n" +
                                "  \"duration\": -100\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }
}
