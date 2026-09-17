package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    public static final int MAX_FILM_DESCRIPTION_LENGTH = 200;
    public static final LocalDate MIN_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
}
