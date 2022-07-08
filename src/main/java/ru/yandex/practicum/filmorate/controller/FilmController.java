package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final DirectorService directorService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Request all films");
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Long id) {
        log.info("Request film by id = {}", id);
        return filmService.getFilmById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Request to add film {}", film);
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Request to change film {}", film);
        return filmService.updateFilm(film);
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable Long id){
        log.info("Request to delete film by id = {}", id);
        filmService.deleteFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Request from user id = {} put like to film id = {}", userId, id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteMapping(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Request from user id = {} delete like to film id = {}", userId, id);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> popularFilms(@RequestParam(defaultValue = "10") Integer count,
                                         @RequestParam(defaultValue = "-1") Integer genreId,
                                         @RequestParam(defaultValue = "-1") Integer year) {
        log.info("Request best films, count = {}, genreId = {}, year = {}", count, genreId, year);
        return filmService.getFilmsByRating(count, genreId, year);
    }

    @GetMapping("/common")
    public Collection<Film> commonFilms(@RequestParam Long userId, Long friendId) {
        log.info("Request common films of users with id {} and id {}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    // GET /films/director/{directorId}?sortBy=year или /films/director/{directorId}?sortBy=likes
    @GetMapping("/director/{directorId}")
    public List<Film> getSortedFilmsByYearOrDirector(@PathVariable @Positive Long directorId,
                                                     @RequestParam Optional<String> sortBy) {
        log.info("Received a request to get sorted films by director id = {}", directorId);
        return directorService.getSortedFilmsByDirectorId(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<Film> search(
            @RequestParam(required = false) String query, String by) {
        log.info("Request search films, query = {}, by = {}", query, by);
        return filmService.search(query, by);
    }
}
