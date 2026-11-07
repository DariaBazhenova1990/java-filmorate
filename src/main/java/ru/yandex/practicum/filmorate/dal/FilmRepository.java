package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.InternalServerException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private static final String FIND_ALL_QUERY =
            "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa m ON f.mpa_id = m.id;";
    private static final String FIND_BY_ID_QUERY =
            "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa m ON f.mpa_id = m.id WHERE f.id = ?;";
    private static final String FIND_POPULAR_QUERY =
            "SELECT f.*, m.name AS mpa_name FROM films f " +
                    "JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "GROUP BY f.id, m.name " +
                    "ORDER BY COUNT(fl.user_id) DESC " +
                    "LIMIT ?";
    private static final String INSERT_QUERY =
            "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?);";
    private static final String UPDATE_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?;";
    private static final String ADD_LIKE_QUERY =
            "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?);";
    private static final String REMOVE_LIKE_QUERY =
            "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?;";
    private static final String FIND_GENRES_BY_FILM_ID_QUERY =
            "SELECT g.id, g.name " +
                    "FROM film_genres fg " +
                    "JOIN genres g ON fg.genre_id = g.id " +
                    "WHERE fg.film_id = ? " +
                    "ORDER BY g.id ASC;";
    private final JdbcTemplate jdbc;
    private final FilmGenresRepository filmGenresRepository;

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper, FilmGenresRepository filmGenresRepository) {
        super(jdbc, mapper, Film.class);
        this.jdbc = jdbc;
        this.filmGenresRepository = filmGenresRepository;
    }

    public List<Film> findAll() {
        List<Film> films = findMany(FIND_ALL_QUERY);
        films.forEach(this::loadGenres);
        return films;
    }

    public Film create(Film film) {
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        filmGenresRepository.update(film);

        return findById(id).orElseThrow(() -> new InternalServerException("Ошибка при создании фильма"));
    }

    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        filmGenresRepository.update(film);

        return findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));
    }

    public Optional<Film> findById(long filmId) {
        Optional<Film> filmOpt = findOne(FIND_BY_ID_QUERY, filmId);
        filmOpt.ifPresent(this::loadGenres);
        return filmOpt;
    }

    public Film likeFilm(long id, long userId) {
        update(ADD_LIKE_QUERY, id, userId);
        return findById(id).orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public Film unlikeFilm(Long id, Long userId) {
        update(REMOVE_LIKE_QUERY, id, userId);
        return findById(id).orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public List<Film> getPopular(int count) {
        List<Film> films = findMany(FIND_POPULAR_QUERY, count);
        films.forEach(this::loadGenres);
        return films;
    }

    private void loadGenres(Film film) {
        List<Genre> genresList = jdbc.query(FIND_GENRES_BY_FILM_ID_QUERY, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());

        film.setGenres(new LinkedHashSet<>(genresList));
    }
}
