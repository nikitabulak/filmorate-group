package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class Director implements Comparable<Director> {
    Long id;
    @NotBlank
    String name;

    @Override
    public int compareTo(Director o) {
        return this.id.compareTo(o.id);
    }
}
