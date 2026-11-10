package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FriendRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.UserDto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto.UserDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    public UserService(UserRepository userRepository, FriendRepository friendRepository) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
    }

    public Collection<UserDto> getAll() {
        log.info("Получение списка всех пользователей");
        return userRepository.findAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(long id) {
        log.info("Получение пользователя по ID {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ошибка поиска: не найден пользователь с ID: {}", id);
                    return new NotFoundException("Пользователь с id " + id + " не найден");
                });
        return UserMapper.mapToUserDto(user);
    }

    public UserDto create(NewUserRequest newUser) {
        log.info("Получен запрос на создание пользователя с логином: {}", newUser.getLogin());
        checkAndSetName(newUser);

        User user = UserMapper.mapToUser(newUser);
        user = userRepository.create(user);
        log.info("Пользователь успешно создан. Присвоен ID: {}", user.getId());
        return UserMapper.mapToUserDto(user);
    }

    private void checkAndSetName(NewUserRequest user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("У пользователя с логином {} не указано имя. В качестве имени установлен логин.", user.getLogin());
        }
    }

    public UserDto update(UpdateUserRequest updateRequest) {
        long userId = updateRequest.getId();
        log.info("Получен запрос на обновление пользователя с ID: {}", userId);
        User user = userRepository.findById(userId)
                .map(userToUpdate -> UserMapper.updateUserFields(userToUpdate, updateRequest))
                .orElseThrow(() -> {
                            log.warn("Ошибка обновления пользователя: пользователь с ID {} не найден", userId);
                            return new NotFoundException("Пользователь с id = " + userId + " не найден");
                        }
                );
        user = userRepository.update(user);
        log.info("Пользователь успешно обновлен. Новые значения: {}", updateRequest);
        return UserMapper.mapToUserDto(user);
    }

    public void doFriends(long id, long friendId) {
        log.info("Получен запрос на добавление в друзья. ID пользователя: {}. ID друга: {}", id, friendId);
        isValidUserPair(id, friendId);
        friendRepository.doFriends(id, friendId);
        log.info("Заявка в друзья отправлена. ID пользователя: {}. ID друга: {}", id, friendId);
    }

    public void undoFriends(Long id, Long friendId) {
        log.info("Получен запрос на удаление из друзей. ID пользователя: {}. ID друга: {}", id, friendId);
        isValidUserPair(id, friendId);
        friendRepository.undoFriends(id, friendId);
        log.info("Запрос на удаление из друзей исполнен. ID пользователя: {}. ID друга: {}", id, friendId);
    }

    public Collection<UserDto> getFriends(Long id) {
        log.info("Получение списка друзей для пользователя с ID {}", id);
        if (userRepository.findById(id).isEmpty()) {
            log.warn("Ошибка поиска: не найден пользователь с ID: {}", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return friendRepository.getFriends(id)
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public Collection<UserDto> getCommonFriends(Long id, Long otherId) {
        log.info("Получение списка общих друзей для пользователей с ID {} и ID {}", id, otherId);
        isValidUserPair(id, otherId);
        return friendRepository.getCommonFriends(id, otherId)
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    private void isValidUserPair(long id, long friendId) {
        Optional<User> user = userRepository.findById(id);
        Optional<User> friend = userRepository.findById(friendId);

        if (user.isEmpty() || friend.isEmpty()) {
            List<Long> missingIds = new ArrayList<>();
            if (user.isEmpty()) missingIds.add(id);
            if (friend.isEmpty()) missingIds.add(friendId);
            log.warn("Ошибка поиска: не найдены пользователи с ID: {}", missingIds);
            throw new NotFoundException("Пользователи с id " + missingIds + " не найдены");
        }

        if (user.equals(friend)) {
            log.warn("Ошибка бизнес-логики: пользователи {} и {} не уникальны", id, friendId);
            throw new ValidationException("Пользователи не уникальны. Переданные ID: " + id + ", " + friendId);
        }
    }
}
