package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class GenreRepository extends BaseRepository<Genre> {
    private static final String FIND_ALL_QUERY =
            "SELECT * FROM genres;";
    private static final String FIND_GENRE_BY_ID_QUERY =
            "SELECT * FROM genres WHERE id = ?;";
    private static final String FIND_ALL_BY_IDS_QUERY =
            "SELECT * FROM genres WHERE id IN (:genreIds)";
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public GenreRepository(
            JdbcTemplate jdbc,
            RowMapper<Genre> mapper,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        super(jdbc, mapper, Genre.class);
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<Genre> getAllGenres() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Genre> getGenreById(int id) {
        return findOne(FIND_GENRE_BY_ID_QUERY, id);
    }

    public List<Genre> getManyGenresByIds(List<Integer> genreIds) {
        if (genreIds.isEmpty()) return Collections.emptyList();
        MapSqlParameterSource parameters = new MapSqlParameterSource("genreIds", genreIds);
        return namedParameterJdbcTemplate.query(FIND_ALL_BY_IDS_QUERY, parameters, mapper);
    }

}
