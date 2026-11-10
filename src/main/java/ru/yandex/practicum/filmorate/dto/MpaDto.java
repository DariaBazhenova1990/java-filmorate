package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MpaDto {
    @NotNull(message = "MPA рейтинг должен быть указан")
    private int id;

    private String name;
}
