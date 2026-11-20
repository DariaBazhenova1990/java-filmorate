package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exceptions.InternalServerException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
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
    private static final String FIND_BY_FILMS_ID_QUERY =
            "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa m ON f.mpa_id = m.id WHERE f.id IN (:filmIds);";
    private static final String FIND_BY_DIRECTOR_SORT_BY_YEAR_QUERY =
            "SELECT f.*, m.name AS mpa_name FROM films f " +
                    "JOIN mpa m ON f.mpa_id = m.id " +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY EXTRACT(YEAR FROM f.release_date) ASC";
    private static final String FIND_BY_DIRECTOR_SORT_BY_LIKES_QUERY =
            "SELECT f.*, m.name AS mpa_name FROM films f " +
                    "JOIN mpa m ON f.mpa_id = m.id " +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.id, m.name " +
                    "ORDER BY COUNT(fl.user_id) DESC";
    private static final String INSERT_QUERY =
            "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?);";
    private static final String UPDATE_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?;";
    private static final String ADD_LIKE_QUERY =
            "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?);";
    private static final String FIND_ALL_LIKES_QUERY =
            "SELECT user_id, film_id FROM film_likes;";
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
    private static final String FIND_DIRECTORS_BY_FILMS_ID_QUERY =
            "SELECT d.id, d.name, fd.film_id " +
                    "FROM directors d " +
                    "JOIN film_directors fd ON d.id = fd.director_id " +
                    "WHERE fd.film_id IN (:filmIds) " +
                    "ORDER BY fd.film_id, d.id";
    private static final String FIND_DIRECTORS_FOR_ONE_FILM_ID_QUERY =
            "SELECT d.id, d.name " +
                    "FROM directors d " +
                    "JOIN film_directors fd ON d.id = fd.director_id " +
                    "WHERE fd.film_id = ? " +
                    "ORDER BY d.id";
    private static final String FIND_USER_LIKES_FOR_FILM_ID_QUERY =
            "SELECT user_id FROM film_likes WHERE film_id = ?;";
    private static final String FIND_USER_LIKES_BY_FILMS_ID_QUERY =
            "SELECT film_id, user_id FROM film_likes WHERE film_id IN (:filmIds);";

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
        loadGenresToFilms(films);
        loadDirectorsToFilms(films);
        return loadUserLikesToFilms(films);
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
        film.ifPresent(f -> {
            loadGenresToOneFilm(f);
            loadDirectorsToOneFilm(f);
            loadUserLikesToOneFilm(f);
        });

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

    public List<Film> getPopular(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.*, m.name AS mpa_name FROM films f " +
                        "JOIN mpa m ON f.mpa_id = m.id " +
                        "LEFT JOIN film_likes fl ON f.id = fl.film_id "
        );

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append("JOIN film_genres fg ON f.id = fg.film_id ");
            conditions.add("fg.genre_id = ?");
            params.add(genreId);
        }

        if (year != null) {
            conditions.add("EXTRACT(YEAR FROM f.release_date) = ?");
            params.add(year);
        }

        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
        }

        sql.append("GROUP BY f.id, m.name ")
                .append("ORDER BY COUNT(DISTINCT fl.user_id) DESC ")
                .append("LIMIT ?");
        params.add(count);

        List<Film> films = findMany(sql.toString(), params.toArray());
        loadGenresToFilms(films);
        loadDirectorsToFilms(films);
        return loadUserLikesToFilms(films);
    }

    public List<Film> getFilmsByDirector(long directorId, String sortBy) {
        String query = "likes".equals(sortBy)
                ? FIND_BY_DIRECTOR_SORT_BY_LIKES_QUERY
                : FIND_BY_DIRECTOR_SORT_BY_YEAR_QUERY;

        List<Film> films = findMany(query, directorId);
        loadGenresToFilms(films);
        return loadDirectorsToFilms(films);
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

    public List<Film> loadDirectorsToFilms(List<Film> films) {
        if (films.isEmpty()) return films;

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        MapSqlParameterSource parameters = new MapSqlParameterSource("filmIds", filmIds);

        List<Map.Entry<Long, Director>> directorEntries = namedParameterJdbcTemplate.query(
                FIND_DIRECTORS_BY_FILMS_ID_QUERY,
                parameters,
                (rs, rowNum) -> {
                    Director director = new Director();
                    director.setId(rs.getLong("id"));
                    director.setName(rs.getString("name"));
                    Long filmId = rs.getLong("film_id");
                    return new AbstractMap.SimpleImmutableEntry<>(filmId, director);
                }
        );

        Map<Long, Set<Director>> directorsByFilmId = directorEntries.stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toCollection(LinkedHashSet::new))
                ));

        films.forEach(film -> film.setDirectors(
                directorsByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>())
        ));

        return films;
    }

    public void loadDirectorsToOneFilm(Film film) {
        Set<Director> directors = new LinkedHashSet<>(
                jdbcTemplate.query(
                        FIND_DIRECTORS_FOR_ONE_FILM_ID_QUERY,
                        (rs, rowNum) -> {
                            Director director = new Director();
                            director.setId(rs.getLong("id"));
                            director.setName(rs.getString("name"));
                            return director;
                        },
                        film.getId()
                )
        );
        film.setDirectors(directors);
    }

    public void loadUserLikesToOneFilm(Film film) {
        Set<Long> userLikes = new HashSet<>(
                jdbcTemplate.queryForList(FIND_USER_LIKES_FOR_FILM_ID_QUERY, Long.class, film.getId())
        );
        film.setUserLikes(userLikes);
    }

    public List<Film> loadUserLikesToFilms(List<Film> films) {
        if (films.isEmpty()) return films;

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        MapSqlParameterSource parameters = new MapSqlParameterSource("filmIds", filmIds);

        List<Map.Entry<Long, Long>> likeEntries = namedParameterJdbcTemplate.query(
                FIND_USER_LIKES_BY_FILMS_ID_QUERY,
                parameters,
                (rs, rowNum) -> new AbstractMap.SimpleImmutableEntry<>(
                        rs.getLong("film_id"),
                        rs.getLong("user_id")
                )
        );

        Map<Long, Set<Long>> likesByFilmId = likeEntries.stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                ));

        films.forEach(film -> film.setUserLikes(
                likesByFilmId.getOrDefault(film.getId(), new HashSet<>())
        ));

        return films;
    }

    public Map<Long, Set<Long>> getAllUserLikes() {
        Map<Long, Set<Long>> userLikes = new HashMap<>();

        jdbcTemplate.query(FIND_ALL_LIKES_QUERY, rs -> {
            long userId = rs.getLong("user_id");
            long filmId = rs.getLong("film_id");
            userLikes.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
        });

        return userLikes;
    }

    public List<Film> getFilmsByIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyList();
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource("filmIds", filmIds);

        List<Film> films = namedParameterJdbcTemplate.query(FIND_BY_FILMS_ID_QUERY, parameters, mapper);

        loadGenresToFilms(films);
        loadDirectorsToFilms(films);
        return loadUserLikesToFilms(films);
    }

}
