package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;

@Data
public class User {
    private Long id;
    @NotNull @Email private String email;
    @Pattern(regexp = "\\S+") private String login;
    private String name;
    @NotNull @Past private LocalDate birthday;
    //private Set<Long> friends = new HashSet<>();

    @JsonCreator
    public User(Long id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

    public String getName() {
        if (name == null || name.isBlank()) return login;
        else return name;
    }
    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("email", email);
        values.put("login", login);
        values.put("name", getName());
        values.put("birthday", birthday);
        return values;
    }
}
