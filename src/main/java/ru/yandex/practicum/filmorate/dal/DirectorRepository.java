package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.InternalServerException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DirectorRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Director> directorRowMapper;

    public List<Director> findAll() {
        String sql = "SELECT id, name FROM directors ORDER BY id";
        return jdbcTemplate.query(sql, directorRowMapper);
    }

    public Optional<Director> findById(long id) {
        String sql = "SELECT id, name FROM directors WHERE id = ?";
        List<Director> directors = jdbcTemplate.query(sql, directorRowMapper, id);
        return directors.stream().findFirst();
    }

    public List<Director> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String sql = "SELECT id, name FROM directors WHERE id IN (" +
                String.join(",", Collections.nCopies(ids.size(), "?")) + ")";

        return jdbcTemplate.query(sql, directorRowMapper, ids.toArray());
    }

    public Director create(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new InternalServerException("Не удалось получить ID созданного режиссёра");
        }

        director.setId(key.longValue());
        return director;
    }

    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql, director.getName(), director.getId());
        if (rowsUpdated == 0) {
            throw new NotFoundException("Режиссёр с id = " + director.getId() + " не найден");
        }
        return director;
    }

    public void delete(long id) {
        String sql = "DELETE FROM directors WHERE id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, id);
        if (rowsDeleted == 0) {
            throw new NotFoundException("Режиссёр с id = " + id + " не найден");
        }
    }
}