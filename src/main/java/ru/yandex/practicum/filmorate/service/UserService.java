package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getAll() {
        log.info("Получение списка всех пользователей");
        return userStorage.getAll();
    }

    public User create(User user) {
        log.info("Получен запрос на создание пользователя с логином: {}", user.getLogin());
        checkAndSetName(user);
        User createdUser = userStorage.create(user);
        log.info("Пользователь успешно создан. Присвоен ID: {}", createdUser.getId());
        return createdUser;
    }

    public User update(User newUser) {
        log.info("Получен запрос на обновление пользователя с ID: {}", newUser.getId());
        if (newUser.getId() == null) {
            log.warn("Ошибка обновления пользователя: не указан ID");
            throw new ValidationException("Id должен быть указан");
        }
        if (userStorage.findById(newUser.getId()).isEmpty()) {
            log.warn("Ошибка обновления пользователя: пользователь с ID {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }
        checkAndSetName(newUser);
        User oldUser = userStorage.update(newUser);
        log.info("Пользователь с ID: {} успешно обновлен", oldUser.getId());
        return oldUser;
    }

    private void checkAndSetName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("У пользователя с логином {} не указано имя. В качестве имени установлен логин.", user.getLogin());
        }
    }

    public User doFriends(Long id, Long friendId) {
        log.info("Получен запрос на добавление в друзья. ID пользователя: {}. ID друга: {}", id, friendId);
        isValidUserPair(id, friendId);
        return userStorage.doFriends(id, friendId);
    }

    public User undoFriends(Long id, Long friendId) {
        log.info("Получен запрос на удаление из друзей. ID пользователя: {}. ID друга: {}", id, friendId);
        isValidUserPair(id, friendId);
        return userStorage.undoFriends(id, friendId);
    }

    public Collection<User> getFriends(Long id) {
        log.info("Получение списка друзей для пользователя с ID {}", id);
        if (userStorage.findById(id).isEmpty()) {
            log.warn("Ошибка поиска: не найден пользователь с ID: {}", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return userStorage.getFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        log.info("Получение списка общих друзей для пользователей с ID {} и ID {}", id, otherId);
        isValidUserPair(id, otherId);
        return userStorage.getCommonFriends(id, otherId);
    }

    private void isValidUserPair(Long id, Long friendId) {
        Optional<User> user = userStorage.findById(id);
        Optional<User> friend = userStorage.findById(friendId);

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
