package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaRepository extends BaseRepository<Mpa> {
    private static final String FIND_ALL_QUERY =
            "SELECT * FROM mpa ORDER BY id ASC;";
    private static final String FIND_MPA_BY_ID_QUERY =
            "SELECT * FROM mpa WHERE id = ?;";

    public MpaRepository(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper, Mpa.class);
    }

    public List<Mpa> getAllMpa() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Mpa> getMpaById(int id) {
        return findOne(FIND_MPA_BY_ID_QUERY, id);
    }
}
