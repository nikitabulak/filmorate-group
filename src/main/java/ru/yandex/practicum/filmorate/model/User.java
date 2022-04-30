package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {
    private int id;
    @NotNull @Email private String email;
    @NotNull @NotBlank private String login;
    @NotNull @NotBlank private String name;
    @NotNull @Past private LocalDate birthday;
}
