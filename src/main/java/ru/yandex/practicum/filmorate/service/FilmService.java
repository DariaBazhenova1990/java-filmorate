package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.FilmDto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmDto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.FilmDto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final FilmGenresRepository filmGenresRepository;
    private final DirectorRepository directorRepository;
    private final FilmDirectorsRepository filmDirectorsRepository;
    private final FeedService feedService;

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
        List<Director> directors = checkAndGetDirectors(newFilm.getDirectors());

        Film film = FilmMapper.mapToFilm(newFilm);

        film = filmRepository.create(film);
        film = filmGenresRepository.update(film, genres);
        film = filmDirectorsRepository.update(film, directors);

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

        for (GenreDto genre : genres) {
            genreIds.add(genre.getId());
        }

        List<Genre> existedGenres = genreRepository.getManyGenresByIds(genreIds);

        if (existedGenres.size() != genreIds.size()) {
            log.warn("Некоторые жанры не найдены");
            throw new NotFoundException("Некоторые жанры не найдены");
        }

        return existedGenres;
    }

    private List<Director> checkAndGetDirectors(Set<DirectorDto> directors) {
        if (directors == null || directors.isEmpty()) {
            return null;
        }

        List<Long> directorIds = directors.stream()
                .map(DirectorDto::getId)
                .toList();

        List<Director> existingDirectors = directorRepository.findAllByIds(directorIds);

        if (existingDirectors.size() != directorIds.size()) {
            log.warn("Некоторые режиссёры не найдены");
            throw new NotFoundException("Некоторые режиссёры не найдены");
        }

        return existingDirectors;
    }

    public FilmDto update(UpdateFilmRequest updateRequest) {
        long filmId = updateRequest.getId();
        log.info("Получен запрос на обновление фильма с ID: {}", filmId);

        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> {
                    log.warn("Ошибка обновления фильма: фильм с ID {} не найден", filmId);
                    return new NotFoundException("Фильм с id = " + filmId + " не найден");
                });

        List<Genre> genres = null;
        if (updateRequest.hasGenres()) {
            genres = checkAndGetGenres(updateRequest.getGenres());
        }

        List<Director> directors = null;
        if (updateRequest.hasDirectors()) {
            directors = checkAndGetDirectors(updateRequest.getDirectors());
        }

        film = FilmMapper.updateFilmFields(film, updateRequest);
        film = filmRepository.update(film);

        if (updateRequest.hasGenres()) {
            film = filmGenresRepository.update(film, genres);
        }

        film = filmDirectorsRepository.update(film, directors);

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
            log.warn("Ошибка добавления лайка фильма: пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        Film likedFilm = filmRepository.likeFilm(id, userId);
        log.info("Пользователь с ID: {} успешно лайкнул фильм с ID {}", userId, id);

        feedService.addEvent(userId, EventType.LIKE, Operation.ADD, id);
        log.info("Добавлено в ленту: пользователь с ID {} лайкнул фильм с ID {}", userId, id);

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

        feedService.addEvent(userId, EventType.LIKE, Operation.REMOVE, id);
        log.info("Добавлено в ленту: пользователь с ID {} отменил лайк фильм с ID {}", userId, id);

        return FilmMapper.mapToFilmDto(unlikedFilm);
    }

    public List<FilmDto> getPopular(int count, Integer genreId, Integer year) {
        log.info("Получение списка популярных фильмов: count={}, genreId={}, year={}", count, genreId, year);
        return filmRepository.getPopular(count, genreId, year)
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

    public List<FilmDto> getFilmsByDirector(long directorId, String sortBy) {
        log.info("Получение списка фильмов режиссёра с ID: {}, сортировка: {}", directorId, sortBy);

        if (directorRepository.findById(directorId).isEmpty()) {
            log.warn("Ошибка поиска: режиссёр с ID {} не найден", directorId);
            throw new NotFoundException("Режиссёр с id = " + directorId + " не найден");
        }

        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            log.warn("Некорректный параметр сортировки: {}", sortBy);
            throw new ValidationException("Некорректный параметр сортировки sortBy: " + sortBy);
        }

        return filmRepository.getFilmsByDirector(directorId, sortBy)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getRecommendations(long userId) {
        log.info("Получение рекомендаций по фильмам для пользователя с ID {}", userId);
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("Ошибка поиска: не найден пользователь с ID: {}", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        // Получаем карту всех лайков: Map<UserId, Set<FilmId>>
        Map<Long, Set<Long>> userLikes = filmRepository.getAllUserLikes();

        // Возвращаем пустой список для пользователя без лайков
        Set<Long> targetUserLikes = userLikes.get(userId);
        if (targetUserLikes == null || targetUserLikes.isEmpty()) {
            return Collections.emptyList();
        }

        // Строим матрицу частот совместных лайков: Map<FilmId1, Map<FilmId2, Frequency>>
        Map<Long, Map<Long, Integer>> freqMatrix = new HashMap<>();

        for (Map.Entry<Long, Set<Long>> entry : userLikes.entrySet()) {
            if (entry.getKey().equals(userId)) {
                continue;
            }

            Set<Long> likedFilms = entry.getValue();
            if (likedFilms == null || likedFilms.isEmpty()) {
                continue;
            }

            for (Long film1 : likedFilms) {
                Map<Long, Integer> row = freqMatrix.computeIfAbsent(film1, k -> new HashMap<>());
                for (Long film2 : likedFilms) {
                    if (!film1.equals(film2)) {
                        row.merge(film2, 1, Integer::sum);
                    }
                }
            }
        }

        // Считаем вес не лайкнутых фильмов
        Map<Long, Integer> filmScores = new HashMap<>();

        for (Long likedFilm : targetUserLikes) {
            Map<Long, Integer> relatedFilms = freqMatrix.get(likedFilm);
            if (relatedFilms == null) {
                continue;
            }

            for (Map.Entry<Long, Integer> entry : relatedFilms.entrySet()) {
                Long candidateFilmId = entry.getKey();
                Integer frequency = entry.getValue();

                if (!targetUserLikes.contains(candidateFilmId)) {
                    filmScores.merge(candidateFilmId, frequency, Integer::sum);
                }
            }
        }

        if (filmScores.isEmpty()) {
            return Collections.emptyList();
        }

        return filmRepository.getFilmsByIds(new ArrayList<>(filmScores.keySet()))
                .stream()
                .sorted(Comparator.comparingInt((Film f) -> filmScores.get(f.getId())).reversed()
                        .thenComparingLong(Film::getId))
                .map(FilmMapper::mapToFilmDto)
                .toList();

    }

    public List<FilmDto> searchFilms(String query, List<SearchBy> by) {
        log.info("Поиск фильма по ключевому слову {} и по критерию {}", query, by);
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        return filmRepository.searchFilms(query, by)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public void delete(long id) {
        log.info("Получен запрос на удаление фильма с id = {}", id);

        if (id <= 0) {
            log.warn("Ошибка валидации при удалении: id должен быть указан");
            throw new ValidationException("Id для удаления должен быть указан");
        }

        boolean isDeleted = filmRepository.delete(id);

        if (!isDeleted) {
            log.warn("Ошибка удаления: фильм с id = {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }

        log.info("Фильм с id = {} успешно удален", id);
    }
}
