package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public Collection<Film> getAll() {
        log.info("Получение списка всех фильмов");
        return filmStorage.getAll();
    }

    public Film create(Film film) {
        log.info("Получен запрос на добавление нового фильма: {}", film.getName());
        Film createdFilm = filmStorage.create(film);
        log.info("Фильм успешно добавлен. Присвоен ID: {}", createdFilm.getId());
        return createdFilm;
    }

    public Film update(Film newFilm) {
        log.info("Получен запрос на обновление фильма с ID: {}", newFilm.getId());
        if (newFilm.getId() == null) {
            log.warn("Ошибка обновления фильма: не указан ID");
            throw new ValidationException("Id должен быть указан");
        }
        if (filmStorage.findById(newFilm.getId()).isEmpty()) {
            log.warn("Ошибка обновления фильма: фильм с ID {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
        Film oldFilm = filmStorage.update(newFilm);
        log.info("Фильм с ID: {} успешно обновлен", oldFilm.getId());
        return oldFilm;
    }
}
