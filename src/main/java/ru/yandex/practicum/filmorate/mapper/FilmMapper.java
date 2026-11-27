package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.FilmDto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmDto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.FilmDto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (request.getGenres() != null) {
            film.setGenres(request.getGenres().stream()
                    .map(genreDto -> {
                        Genre genre = new Genre();
                        genre.setId(genreDto.getId());
                        return genre;
                    })
                    .collect(Collectors.toSet()));
        }

        if (request.getDirectors() != null) {
            film.setDirectors(request.getDirectors().stream()
                    .map(directorDto -> {
                        Director director = new Director();
                        director.setId(directorDto.getId());
                        return director;
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        if (request.getMpa() != null) {
            Mpa mpa = new Mpa();
            mpa.setId(request.getMpa().getId());
            film.setMpa(mpa);
        }

        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setUserLikes(film.getUserLikes());

        if (film.getGenres() != null) {
            dto.setGenres(film.getGenres().stream()
                    .map(GenreMapper::mapToGenreDto)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        if (film.getDirectors() != null) {
            dto.setDirectors(film.getDirectors().stream()
                    .map(director -> {
                        DirectorDto directorDto = new DirectorDto();
                        directorDto.setId(director.getId());
                        directorDto.setName(director.getName());
                        return directorDto;
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        if (film.getMpa() != null) {
            MpaDto mpaDto = new MpaDto();
            mpaDto.setId(film.getMpa().getId());
            mpaDto.setName(film.getMpa().getName());
            dto.setMpa(mpaDto);
        }

        return dto;
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest filmUpdate) {
        if (filmUpdate.hasName()) {
            film.setName(filmUpdate.getName());
        }
        if (filmUpdate.hasDescription()) {
            film.setDescription(filmUpdate.getDescription());
        }
        if (filmUpdate.hasReleaseDate()) {
            film.setReleaseDate(filmUpdate.getReleaseDate());
        }
        if (filmUpdate.hasDuration()) {
            film.setDuration(filmUpdate.getDuration());
        }
        film.setGenres(filmUpdate.getGenres().stream()
                .map(genreDto -> {
                    Genre genre = new Genre();
                    genre.setId(genreDto.getId());
                    return genre;
                })
                .collect(Collectors.toSet()));
        if (filmUpdate.hasDirectors() && filmUpdate.getDirectors() != null) {
            film.setDirectors(filmUpdate.getDirectors().stream()
                    .map(directorDto -> {
                        Director director = new Director();
                        director.setId(directorDto.getId());
                        return director;
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        if (filmUpdate.hasMpa() && filmUpdate.getMpa() != null) {
            Mpa mpa = new Mpa();
            mpa.setId(filmUpdate.getMpa().getId());
            film.setMpa(mpa);
        }

        return film;
    }
}