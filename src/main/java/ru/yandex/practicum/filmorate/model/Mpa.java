package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Mpa {
    private int id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;
}
