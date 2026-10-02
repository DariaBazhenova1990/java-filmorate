package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

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
}
