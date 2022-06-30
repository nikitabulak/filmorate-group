package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }


    @GetMapping
    public List<Director> getAllDirectors() {
        log.info("Received a request to get all directors");
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable @Positive Long id) {
        log.info("Received a request to get a director by id = {}", id);
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director createDirector(@Valid @RequestBody Director director) {
        log.info("Received a request to create a director = {}", director);
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Received a request to update a director = {}", director);
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void removeDirectorById(@PathVariable @Positive Long id) {
        log.info("Received a request to remove a director by id = {}", id);
        directorService.removeDirectorById(id);
    }
}
