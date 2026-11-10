package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenreDto {
    @NotNull(message = "Жанр должен быть указан")
    private int id;

    private String name;
}
