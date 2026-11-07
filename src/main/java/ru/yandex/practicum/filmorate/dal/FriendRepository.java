package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Repository
public class FriendRepository {
    private static final String DO_FRIENDS_QUERY =
            "INSERT INTO friends(user_id, friend_id) VALUES (?, ?);";
    private static final String UNDO_FRIENDS_QUERY =
            "DELETE FROM friends WHERE user_id = ? AND friend_id=  ?;";
    private static final String FIND_FRIENDS_QUERY =
            "SELECT u.* " +
                    "FROM friends AS f " +
                    "LEFT JOIN users AS u ON f.friend_id = u.id " +
                    "WHERE f.user_id = ? ";
    private static final String FIND_COMMON_FRIENDS_QUERY =
            "SELECT u.* " +
                    "FROM users AS u " +
                    "JOIN friends AS f1 ON u.id = f1.friend_id " +
                    "JOIN friends AS f2 ON f1.friend_id = f2.friend_id " +
                    "WHERE f1.user_id = ? AND f2.user_id = ?";
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userMapper;

    public FriendRepository(JdbcTemplate jdbcTemplate, UserRowMapper userMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
    }

    public void doFriends(long id, long friendId) {
        jdbcTemplate.update(DO_FRIENDS_QUERY, id, friendId);
    }

    public void undoFriends(long id, long friendId) {
        jdbcTemplate.update(UNDO_FRIENDS_QUERY, id, friendId);
    }

    public Collection<User> getFriends(long id) {
        return jdbcTemplate.query(FIND_FRIENDS_QUERY, userMapper, id);
    }

    public Collection<User> getCommonFriends(long id, long otherId) {
        return jdbcTemplate.query(FIND_COMMON_FRIENDS_QUERY, userMapper, id, otherId);
    }
}
