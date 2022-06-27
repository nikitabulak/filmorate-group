package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;

@Data
@NoArgsConstructor
public class Film {
    private Long id;
    @NotBlank private String name;
    @NotBlank @Size(max = 200) private String description;
    @NotNull @Past private LocalDate releaseDate;
    @NotNull @Positive private int duration;
    private Set<Long> likes = new TreeSet<>();
    private Set<Genre> genres;
    @NotNull private Mpa mpa;
    private Long rating;


    public Film(Long id, String name, String description, LocalDate releaseDate, int duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

    public Film(Long id, String name, String description, LocalDate releaseDate, int duration,
                Set<Genre> genres, Mpa mpa) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genres = genres;
        this.mpa = mpa;
    }

    public Film(Long id, String name, String description, LocalDate releaseDate, int duration, Set<Genre> genres,
                Mpa mpa, Long rating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genres = genres;
        this.mpa = mpa;
        this.rating = rating;
    }

    public int getRating(){
        return likes.size();
    }

    public boolean hasLikeFromUser(Long userId) {
        return likes.contains(userId);
    }

    public void addLikeFromUser(Long userId) {
        likes.add(userId);
    }

    public void removeLikeFromUser(Long userId) {
        likes.remove(userId);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", name);
        values.put("description", description);
        values.put("releaseDate", releaseDate);
        values.put("duration", duration);
        return values;
    }
}
