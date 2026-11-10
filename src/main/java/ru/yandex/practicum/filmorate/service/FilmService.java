package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dto.FilmDto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.FilmDto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.FilmDto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final FilmGenresRepository filmGenresRepository;

    public FilmService(
            FilmRepository filmRepository,
            UserRepository userRepository,
            MpaRepository mpaRepository,
            GenreRepository genreRepository,
            FilmGenresRepository filmGenresRepository
    ) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
        this.filmGenresRepository = filmGenresRepository;
    }

    public List<FilmDto> getAll() {
        log.info("Получение списка всех фильмов");
        return filmRepository.findAll()
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto create(NewFilmRequest newFilm) {
        log.info("Получен запрос на добавление нового фильма: {}", newFilm.getName());
        checkMpa(newFilm.getMpa());
        List<Genre> genres = checkAndGetGenres(newFilm.getGenres());
        Film film = FilmMapper.mapToFilm(newFilm);

        film = filmRepository.create(film);
        film = filmGenresRepository.update(film, genres);

        log.info("Фильм успешно добавлен. Присвоен ID: {}", film.getId());

        return FilmMapper.mapToFilmDto(film);
    }

    private void checkMpa(MpaDto mpa) {
        if (mpaRepository.getMpaById(mpa.getId()).isEmpty()) {
            log.warn("Ошибка поиска: MPA с ID {} не найдена", mpa.getId());
            throw new NotFoundException("MPA с id = " + mpa.getId() + " не найдена");
        }
    }

    private List<Genre> checkAndGetGenres(Set<GenreDto> genres) {
        if (genres == null || genres.isEmpty()) {
            return null;
        }

        List<Integer> genreIds = new ArrayList<>();
        for (GenreDto genre : genres) genreIds.add(genre.getId());

        List<Genre> existedGenres = genreRepository.getManyGenresByIds(genreIds);

        if (existedGenres.size() != genreIds.size()) {
            log.warn("Некоторые жанры не найдены");
            throw new NotFoundException("Некоторые жанры не найдены");
        }
        return existedGenres;

    }

    public FilmDto update(UpdateFilmRequest updateRequest) {
        long filmId = updateRequest.getId();
        log.info("Получен запрос на обновление фильма с ID: {}", filmId);
        Film film = filmRepository.findById(filmId)
                .map(filmToUpdate -> FilmMapper.updateFilmFields(filmToUpdate, updateRequest))
                .orElseThrow(() -> {
                            log.warn("Ошибка обновления фильма: фильм с ID {} не найден", filmId);
                            return new NotFoundException("Фильм с id = " + filmId + " не найден");
                        }
                );
        List<Genre> genres = checkAndGetGenres(updateRequest.getGenres());
        film = filmRepository.update(film);
        film = filmGenresRepository.update(film, genres);
        log.info("Фильм с ID: {} успешно обновлен", film.getId());
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto likeFilm(long id, long userId) {
        log.info("Получен запрос на добавление лайка для фильма с ID: {}", id);
        if (filmRepository.findById(id).isEmpty()) {
            log.warn("Ошибка добавления лайка фильма: фильм с ID {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("Ошибка добавления лайка фильма: пользователь с ID {} не найден", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        Film likedFilm = filmRepository.likeFilm(id, userId);
        log.info("Пользователь с ID: {} успешно лайкнул фильм с ID {}", userId, id);
        return FilmMapper.mapToFilmDto(likedFilm);
    }

    public FilmDto unlikeFilm(Long id, Long userId) {
        log.info("Получен запрос на удаление лайка для фильма с ID: {}", id);
        if (filmRepository.findById(id).isEmpty()) {
            log.warn("Ошибка удаления лайка фильма: фильм с ID {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("Ошибка удаления лайка фильма: пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        Film unlikedFilm = filmRepository.unlikeFilm(id, userId);
        log.info("Пользователь с ID: {} успешно отменил лайк фильма с ID {}", userId, id);
        return FilmMapper.mapToFilmDto(unlikedFilm);
    }

    public List<FilmDto> getPopular(int count) {
        log.info("Получение списка популярных фильмов");
        return filmRepository.getPopular(count)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }


    public FilmDto getFilmById(int id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ошибка поиска: Фильм с ID {} не найден", id);
                    return new NotFoundException("Фильм с id = " + id + " не найден");
                });
        return FilmMapper.mapToFilmDto(film);
    }
}
