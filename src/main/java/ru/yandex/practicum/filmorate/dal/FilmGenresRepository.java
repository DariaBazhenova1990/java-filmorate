package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Repository
public class FilmGenresRepository {
    private static final String INSERT_FILM_GENRES_QUERY =
            "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?);";
    private static final String DELETE_FILM_GENRES_QUERY =
            "DELETE FROM film_genres WHERE film_id = ?;";
    private final JdbcTemplate jdbcTemplate;

    public FilmGenresRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film update(Film film, List<Genre> genres) {
        jdbcTemplate.update(DELETE_FILM_GENRES_QUERY, film.getId());

        if (genres != null && !genres.isEmpty()) {
            Set<Genre> uniqueGenres = new LinkedHashSet<>(genres);
            film.setGenres(uniqueGenres);

            List<Genre> genresList = new ArrayList<>(uniqueGenres);

            jdbcTemplate.batchUpdate(
                    INSERT_FILM_GENRES_QUERY,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, film.getId());
                            ps.setInt(2, genresList.get(i).getId());
                        }

                        @Override
                        public int getBatchSize() {
                            return genresList.size();
                        }
                    }
            );
        }

        return film;
    }

}
