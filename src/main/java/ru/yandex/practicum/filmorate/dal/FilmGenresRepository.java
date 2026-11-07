package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.LinkedHashSet;
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

    public Film update(Film film) {
        jdbcTemplate.update(DELETE_FILM_GENRES_QUERY, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = new LinkedHashSet<>(film.getGenres());
            film.setGenres(uniqueGenres);

            for (Genre genre : uniqueGenres) {
                jdbcTemplate.update(INSERT_FILM_GENRES_QUERY, film.getId(), genre.getId());
            }
        }
        return film;
    }
}
