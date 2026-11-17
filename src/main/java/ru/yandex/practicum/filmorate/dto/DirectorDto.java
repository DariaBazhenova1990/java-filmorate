package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DirectorDto {
    private long id;

    @NotBlank(message = "Имя режиссёра не может быть пустым")
    private String name;
}