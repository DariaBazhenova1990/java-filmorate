package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Repository
public class FeedRepository extends BaseRepository<Event> {
    private static final String FIND_BY_USER_ID_QUERY =
            "SELECT * FROM events WHERE user_id = ? ORDER BY event_id ASC;";
    private static final String INSERT_QUERY =
            "INSERT INTO events(timestamp, user_id, event_type, operation, entity_id) " +
                    "VALUES (?, ?, ?, ?, ?);";

    public FeedRepository(JdbcTemplate jdbc, RowMapper<Event> mapper) {
        super(jdbc, mapper, Event.class);
    }

    public List<Event> findByUserId(long userId) {
        return findMany(FIND_BY_USER_ID_QUERY, userId);
    }

    public Event create(Event event) {
        long id = insert(
                INSERT_QUERY,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId()
        );
        event.setEventId(id);
        return event;
    }
}