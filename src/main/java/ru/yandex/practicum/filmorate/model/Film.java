package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMin;

import javax.validation.constraints.*;
import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    private int id;
    @NotBlank private String name;
    @NotBlank @Size(max = 200) private String description;
    @NotNull @Past private LocalDate releaseDate;
    @NotNull @DurationMin(minutes = 0L) private Duration duration;
}
