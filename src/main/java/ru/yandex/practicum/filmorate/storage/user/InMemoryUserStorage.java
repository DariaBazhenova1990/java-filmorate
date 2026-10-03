package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 0;

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        user.setId(++currentId);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User doFriends(Long id, Long friendId) {
        User user = users.get(id);
        User friend = users.get(friendId);
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        user.getFriends().add(friendId);
        if (friend.getFriends() == null) {
            friend.setFriends(new HashSet<>());
        }
        friend.getFriends().add(id);
        return user;
    }

    @Override
    public User undoFriends(Long id, Long friendId) {
        User user = users.get(id);
        User friend = users.get(friendId);
        if (user.getFriends() != null) {
            user.getFriends().remove(friendId);
        }
        if (friend.getFriends() != null) {
            friend.getFriends().remove(id);
        }
        return user;
    }

    @Override
    public Collection<User> getFriends(Long id) {
        return Optional.ofNullable(users.get(id))
                .map(User::getFriends)
                .orElse(Collections.emptySet())
                .stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        User user = users.get(id);
        User otherUser = users.get(otherId);

        return user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(users::get)
                .toList();
    }

    public void deleteAllUsers() {
        users.clear();
        currentId = 0;
    }
}
