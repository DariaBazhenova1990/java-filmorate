package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 0;

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        film.setId(++currentId);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film likeFilm(Long id, Long userId) {
        Film film = films.get(id);
        if (film.getUserLikes() == null) {
            film.setUserLikes(new HashSet<>());
        }
        film.getUserLikes().add(userId);
        return film;
    }

    @Override
    public Film unlikeFilm(Long id, Long userId) {
        Film film = films.get(id);
        if (film.getUserLikes() != null) {
            film.getUserLikes().remove(userId);
        }
        return film;
    }

    @Override
    public Collection<Film> getPopular(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> {
                    int size1 = f1.getUserLikes() == null ? 0 : f1.getUserLikes().size();
                    int size2 = f2.getUserLikes() == null ? 0 : f2.getUserLikes().size();
                    return Integer.compare(size2, size1);
                })
                .limit(count)
                .toList();
    }

    public void deleteAllFilms() {
        films.clear();
        currentId = 0;
    }

}
