package ru.yandex.practicum.filmorate.dto.FilmDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.validator.MinimumDate;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateFilmRequest {
    private long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания не должна быть более 200 символов")
    private String description;

    @NotNull(message = "Дата релиза должна быть указана")
    @MinimumDate(value = "1895-12-28", message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    @Valid
    private Set<GenreDto> genres;

    @Valid
    private MpaDto mpa;

    public boolean hasName() {
        return name != null && !name.isBlank();
    }

    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    public boolean hasReleaseDate() {
        return releaseDate != null;
    }

    public boolean hasDuration() {
        return duration != null;
    }

    public boolean hasGenres() {
        return genres != null;
    }

    public boolean hasMpa() {
        return mpa != null;
    }
}
