package ru.yandex.practicum.filmorate;

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
public class UserControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

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
}
