package ru.yandex.practicum.filmorate.repositoryTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.FilmGenresRepository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmRepository.class, FilmRowMapper.class, FilmGenresRepository.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmRepositoryTests {

    private final FilmRepository filmStorage;

    @Test
    public void testFindFilmById() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("All ages");

        Film film = new Film();
        film.setName("Film 1");
        film.setDescription("Description 1");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);

        filmStorage.create(film);
        long id = film.getId();

        Optional<Film> filmOptional = filmStorage.findById(id);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", id)
                );
    }

    @Test
    public void testCreateFilm() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("All ages");

        Film film = new Film();
        film.setName("New Film");
        film.setDescription("New Description");
        film.setReleaseDate(LocalDate.of(2021, 1, 1));
        film.setDuration(90);
        film.setMpa(mpa);

        Film savedFilm = filmStorage.create(film);

        assertThat(savedFilm).isNotNull();
        assertThat(savedFilm.getId()).isGreaterThan(0);
        assertThat(savedFilm.getName()).isEqualTo("New Film");
        assertThat(savedFilm.getDescription()).isEqualTo("New Description");
        assertThat(savedFilm.getReleaseDate()).isEqualTo(LocalDate.of(2021, 1, 1));
        assertThat(savedFilm.getDuration()).isEqualTo(90);
    }

    @Test
    public void testFindAllFilms() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("All ages");

        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(120);
        film1.setMpa(mpa);
        filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2020, 2, 1));
        film2.setDuration(150);
        film2.setMpa(mpa);
        filmStorage.create(film2);

        List<Film> films = filmStorage.findAll();

        assertThat(films.size()).isEqualTo(2);
    }

    @Test
    public void testUpdateFilm() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("All ages");

        Film film = new Film();
        film.setName("Old Name");
        film.setDescription("Old Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        film.setMpa(mpa);
        Film savedFilm = filmStorage.create(film);
        long id = savedFilm.getId();

        Film updatedFilm = new Film();
        updatedFilm.setId(id);
        updatedFilm.setName("New Name");
        updatedFilm.setDescription("New Description");
        updatedFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        updatedFilm.setDuration(200);
        updatedFilm.setMpa(mpa);

        Film result = filmStorage.update(updatedFilm);

        assertThat(result).hasFieldOrPropertyWithValue("id", id);
        assertThat(result).hasFieldOrPropertyWithValue("name", "New Name");
        assertThat(result).hasFieldOrPropertyWithValue("description", "New Description");
        assertThat(result).hasFieldOrPropertyWithValue(
                "releaseDate", LocalDate.of(2021, 1, 1)
        );
        assertThat(result).hasFieldOrPropertyWithValue("duration", 200);
    }
}
