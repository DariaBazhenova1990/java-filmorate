package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmDto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.FilmDto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public List<FilmDto> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@PathVariable("id") int id) {
        return filmService.getFilmById(id);
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody NewFilmRequest newFilm) {
        return filmService.create(newFilm);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody UpdateFilmRequest filmUpdate) {
        return filmService.update(filmUpdate);
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmDto likeFilm(@PathVariable("id") long id, @Valid @PathVariable("userId") long userId) {
        return filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmDto unlikeFilm(@PathVariable("id") long id, @PathVariable("userId") long userId) {
        return filmService.unlikeFilm(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopular(@RequestParam(defaultValue = "10") @Positive int count) {
        return filmService.getPopular(count);
    }

}
