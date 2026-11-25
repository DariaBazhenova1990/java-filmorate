package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmDto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.FilmDto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.SearchBy;
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
    public FilmDto likeFilm(
            @PathVariable("id") long id,
            @Valid @PathVariable("userId") long userId
    ) {
        return filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmDto unlikeFilm(
            @PathVariable("id") long id,
            @PathVariable("userId") long userId
    ) {
        return filmService.unlikeFilm(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopular(
            @RequestParam(defaultValue = "10") @Positive int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) @Min(1895) Integer year
    ) {
        return filmService.getPopular(count, genreId, year);
    }

    @GetMapping("/common")
    public List<FilmDto> getCommonFilms(
            @RequestParam long userId,
            @RequestParam long friendId
    ) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<FilmDto> getFilmsByDirector(
            @PathVariable("directorId") long directorId,
            @RequestParam(name = "sortBy", defaultValue = "year") String sortBy
    ) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<FilmDto> searchFilms(
            @RequestParam String query,
            @RequestParam List<SearchBy> by) {
        return filmService.searchFilms(query, by);
    }

    @DeleteMapping("/{filmId}")
    public void delete(@PathVariable long filmId) {
        filmService.delete(filmId);
    }
}
