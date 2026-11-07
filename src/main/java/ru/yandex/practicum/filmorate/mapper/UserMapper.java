package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.UserDto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {
    public static User mapToUser(NewUserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setName(request.getName());
        user.setBirthday(request.getBirthday());

        return user;
    }

    public static UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthday(user.getBirthday());
        dto.setFriends(user.getFriends());

        return dto;
    }

    public static User updateUserFields(User user, UpdateUserRequest userUpdate) {
        if (userUpdate.hasEmail()) {
            user.setEmail(userUpdate.getEmail());
        }
        if (userUpdate.hasLogin()) {
            user.setLogin(userUpdate.getLogin());
        }
        if (userUpdate.hasName()) {
            user.setName(userUpdate.getName());
        }
        if (userUpdate.hasBirthday()) {
            user.setBirthday(userUpdate.getBirthday());
        }

        return user;
    }
}
