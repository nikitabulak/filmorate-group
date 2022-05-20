package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;
    @NotNull @Email private String email;
    @Pattern(regexp = "\\S+") private String login;
    private String name;
    @NotNull @Past private LocalDate birthday;
    private Set<Long> friends = new HashSet<>();

    public String getName() {
        if (name == null || name.isBlank()) return login;
        else return name;
    }
}
