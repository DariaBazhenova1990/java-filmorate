package ru.yandex.practicum.filmorate.dto.FilmDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class FilmDto {
    private long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<Long> userLikes;
    private Set<GenreDto> genres = new LinkedHashSet<>();

    @JsonProperty("director")
    private Set<DirectorDto> directors = new LinkedHashSet<>();

    private MpaDto mpa;
}