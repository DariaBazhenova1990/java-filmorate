package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exceptions.InternalServerException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

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
    private static final String FIND_GENRES_BY_FILMS_ID_QUERY =
            "SELECT g.id, g.name, fg.film_id " +
                    "FROM genres g " +
                    "JOIN film_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id IN (:filmIds) " +
                    "ORDER BY fg.film_id, g.id";
    private static final String FIND_GENRES_FOR_ONE_FILM_ID_QUERY =
            "SELECT g.id, g.name, fg.film_id " +
                    "FROM genres g " +
                    "JOIN film_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id = ? " +
                    "ORDER BY g.id";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public FilmRepository(
            JdbcTemplate jdbc,
            RowMapper<Film> mapper,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            JdbcTemplate jdbcTemplate,
            GenreRowMapper genreRowMapper
    ) {
        super(jdbc, mapper, Film.class);
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
    }

    public List<Film> findAll() {
        List<Film> films = findMany(FIND_ALL_QUERY);
        return loadGenresToFilms(films);
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

        return findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));
    }

    public Optional<Film> findById(long filmId) {
        Optional<Film> film = findOne(FIND_BY_ID_QUERY, filmId);
        film.ifPresent(this::loadGenresToOneFilm);

        return film;
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
        return loadGenresToFilms(films);
    }

    public List<Film> loadGenresToFilms(List<Film> films) {
        if (films.isEmpty()) return films;

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        MapSqlParameterSource parameters = new MapSqlParameterSource("filmIds", filmIds);

        List<Map.Entry<Long, Genre>> genreEntries = namedParameterJdbcTemplate.query(
                FIND_GENRES_BY_FILMS_ID_QUERY,
                parameters,
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    Long filmId = rs.getLong("film_id");
                    return new AbstractMap.SimpleImmutableEntry<>(filmId, genre);
                }
        );

        Map<Long, Set<Genre>> genresByFilmId = genreEntries.stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toCollection(LinkedHashSet::new))
                ));

        films.forEach(film -> film.setGenres(
                genresByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>())
        ));

        return films;
    }

    public void loadGenresToOneFilm(Film film) {
        Set<Genre> genres = new LinkedHashSet<>(
                jdbcTemplate.query(FIND_GENRES_FOR_ONE_FILM_ID_QUERY, genreRowMapper, film.getId())
        );
        film.setGenres(genres);

    }
}
