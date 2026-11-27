package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DirectorService {

    private final DirectorRepository directorRepository;

    public DirectorService(DirectorRepository directorRepository) {
        this.directorRepository = directorRepository;
    }

    public List<DirectorDto> getAll() {
        log.info("Получение списка всех режиссёров");
        return directorRepository.findAll()
                .stream()
                .map(DirectorMapper::mapToDirectorDto)
                .collect(Collectors.toList());
    }

    public DirectorDto getById(long id) {
        log.info("Получение режиссёра с ID: {}", id);
        Director director = directorRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Режиссёр с ID {} не найден", id);
                    return new NotFoundException("Режиссёр с id = " + id + " не найден");
                });
        return DirectorMapper.mapToDirectorDto(director);
    }

    public DirectorDto create(DirectorDto directorDto) {
        log.info("Создание нового режиссёра: {}", directorDto.getName());
        Director director = DirectorMapper.mapToDirector(directorDto);
        director = directorRepository.create(director);
        log.info("Режиссёр успешно создан с ID: {}", director.getId());
        return DirectorMapper.mapToDirectorDto(director);
    }

    public DirectorDto update(DirectorDto directorDto) {
        log.info("Обновление режиссёра с ID: {}", directorDto.getId());
        Director director = DirectorMapper.mapToDirector(directorDto);
        director = directorRepository.update(director);
        log.info("Режиссёр с ID {} успешно обновлен", director.getId());
        return DirectorMapper.mapToDirectorDto(director);
    }

    public void delete(long id) {
        log.info("Удаление режиссёра с ID: {}", id);
        directorRepository.delete(id);
        log.info("Режиссёр с ID {} успешно удален", id);
    }
}