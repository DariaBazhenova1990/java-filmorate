package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> getAll();

    Film create(Film film);

    Film update(Film film);

    Optional<Film> findById(Long id);

    Film likeFilm(Long id, Long userId);

    Film unlikeFilm(Long id, Long userId);

    Collection<Film> getPopular(int count);
}
