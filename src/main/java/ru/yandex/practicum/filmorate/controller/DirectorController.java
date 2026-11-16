package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public List<DirectorDto> getAll() {
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public DirectorDto getById(@PathVariable("id") long id) {
        return directorService.getById(id);
    }

    @PostMapping
    public DirectorDto create(@Valid @RequestBody DirectorDto directorDto) {
        return directorService.create(directorDto);
    }

    @PutMapping
    public DirectorDto update(@Valid @RequestBody DirectorDto directorDto) {
        return directorService.update(directorDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") long id) {
        directorService.delete(id);
    }
}