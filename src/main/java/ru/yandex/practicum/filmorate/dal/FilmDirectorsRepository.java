package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Repository
public class FilmDirectorsRepository {

    private static final String INSERT_FILM_DIRECTORS_QUERY =
            "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?);";

    private static final String DELETE_FILM_DIRECTORS_QUERY =
            "DELETE FROM film_directors WHERE film_id = ?;";

    private final JdbcTemplate jdbcTemplate;

    public FilmDirectorsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film update(Film film, List<Director> directors) {
        jdbcTemplate.update(DELETE_FILM_DIRECTORS_QUERY, film.getId());

        Set<Director> uniqueDirectors = directors == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(directors);

        film.setDirectors(uniqueDirectors);

        if (!uniqueDirectors.isEmpty()) {
            List<Director> directorsList = new ArrayList<>(uniqueDirectors);

            jdbcTemplate.batchUpdate(
                    INSERT_FILM_DIRECTORS_QUERY,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, film.getId());
                            ps.setLong(2, directorsList.get(i).getId());
                        }

                        @Override
                        public int getBatchSize() {
                            return directorsList.size();
                        }
                    }
            );
        }

        return film;
    }
}