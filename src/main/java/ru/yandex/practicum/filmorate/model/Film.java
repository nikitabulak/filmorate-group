package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMin;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Positive;
import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    private int id;
    @NotNull @NotBlank private String name;
    @NotNull @NotBlank private String description;
    @NotNull @Past private LocalDate releaseDate;
    @NotNull @DurationMin(minutes = 0L) private Duration duration;
}
