package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GengeMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<GenreDto> getAllGenres() {
        log.info("Получение списка всех жанров");
        return genreRepository.getAllGenres()
                .stream()
                .map(GengeMapper::mapToGenreDto)
                .collect(Collectors.toList());
    }

    public GenreDto getGenreById(int id) {
        Genre genre = genreRepository.getGenreById(id)
                .orElseThrow(() -> {
                            log.warn("Ошибка поиска: Жанр с ID {} не найден", id);
                            return new NotFoundException("Жанр с id = " + id + " не найден");
                        }
                );
        return GengeMapper.mapToGenreDto(genre);
    }
}
