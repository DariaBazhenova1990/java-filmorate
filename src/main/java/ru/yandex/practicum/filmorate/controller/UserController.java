package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto.UserDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable("id") long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody NewUserRequest newUser) {
        return userService.create(newUser);
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody UpdateUserRequest userUpdate) {
        return userService.update(userUpdate);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void doFriends(@PathVariable("id") long id, @PathVariable("friendId") long friendId) {
        userService.doFriends(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void undoFriends(@PathVariable("id") long id, @PathVariable("friendId") long friendId) {
        userService.undoFriends(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<UserDto> getFriends(@PathVariable("id") long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<UserDto> getCommonFriends(@PathVariable("id") long id, @PathVariable("otherId") long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public List<FilmDto> getRecommendations(@PathVariable("id") long id) {
        return userService.getRecommendations(id);
    }

}
