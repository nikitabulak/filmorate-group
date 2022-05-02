package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilmControllerTest {

    ConfigurableApplicationContext context;
    String url = "http://localhost:8080/films";
    HttpClient client;

    @BeforeEach
    void beforeEach() {
        context = SpringApplication.run(FilmorateApplication.class);
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void afterEach() {
        SpringApplication.exit(context);
    }

    @Test
    void shouldAddFilm() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers
                .ofString("{\n" +
                        "  \"name\": \"nisi eiusmod\",\n" +
                        "  \"description\": \"adipisicing\",\n" +
                        "  \"releaseDate\": \"1967-03-25\",\n" +
                        "  \"duration\": 100\n" +
                        "}")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldNotAddFilmWithEmptyName() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers
                        .ofString("{\n" +
                                "  \"name\": \"\",\n" +
                                "  \"description\": \"adipisicing\",\n" +
                                "  \"releaseDate\": \"1967-03-25\",\n" +
                                "  \"duration\": 100\n" +
                                "}")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldNotAddFilmWithTooLongDescription() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers
                        .ofString("{\n" +
                                "  \"name\": \"nisi eiusmod\",\n" +
                                "  \"description\": \"qqqqqqqqqqwwwwwwwwwweeeeeeeeeerrrrrrrrrrttttttttttyyyyyyyyyy" +
                                "qqqqqqqqqqqwwwwwwwwwweeeeeeeeeerrrrrrrrrrttttttttttyyyyyyyyyyqqqqqqqqqqwwwwwwwwww" +
                                "wwwwwwwwwweeeeeeeeeerrrrrrrrrrttttttttttyyyyyyyyyyqqqqqqqqqqqqqqqqqqqqqqqqqq\",\n" +
                                "  \"releaseDate\": \"1967-03-25\",\n" +
                                "  \"duration\": 100\n" +
                                "}")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldNotAddFilmWithTooOldDate() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers
                        .ofString("{\n" +
                                "  \"name\": \"nisi eiusmod\",\n" +
                                "  \"description\": \"adipisicing\",\n" +
                                "  \"releaseDate\": \"1400-03-25\",\n" +
                                "  \"duration\": 100\n" +
                                "}")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldNotAddFilmWithNegativeDuration() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers
                        .ofString("{\n" +
                                "  \"name\": \"nisi eiusmod\",\n" +
                                "  \"description\": \"adipisicing\",\n" +
                                "  \"releaseDate\": \"1967-03-25\",\n" +
                                "  \"duration\": -100\n" +
                                "}")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }
}
