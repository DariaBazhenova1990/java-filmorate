package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilmController filmController;

    private static Stream<Arguments> provideInvalidFilms() {
        Film blankName = createValidFilm();
        blankName.setName("   ");

        Film longDescription = createValidFilm();
        longDescription.setDescription("D".repeat(201));

        Film tooOldRelease = createValidFilm();
        tooOldRelease.setReleaseDate(LocalDate.of(1895, 12, 27));

        Film negativeDuration = createValidFilm();
        negativeDuration.setDuration(-1);

        return Stream.of(
                Arguments.of(blankName, "name", "Название не может быть пустым"),
                Arguments.of(longDescription, "description",
                        "Максимальная длина описания не должна быть более 200 символов"),
                Arguments.of(tooOldRelease, "releaseDate",
                        "Дата релиза должна быть не раньше 28 декабря 1895 года"),
                Arguments.of(negativeDuration, "duration",
                        "Продолжительность фильма должна быть положительным числом")
        );
    }

    private static Film createValidFilm() {
        Film film = new Film();
        film.setName("Гладиатор");
        film.setDescription("Генерал, ставший рабом. Раб, ставший гладиатором. Гладиатор, бросивший вызов империи.");
        film.setReleaseDate(LocalDate.of(2000, 5, 1));
        film.setDuration(155);
        return film;
    }

    @BeforeEach
    void setUp() {
        filmController.deleteAllFilms();
    }

    @Test
    void createFilmWithValidData() throws Exception {
        Film film = createValidFilm();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Гладиатор"));
    }

    @ParameterizedTest(name = "{index} ==> Проверка поля ''{1}'' на ошибку: ''{2}''")
    @MethodSource("provideInvalidFilms")
    void failFilmCreationWithInvalidData(Film invalidFilm, String fieldName, String expectedMessage) throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации полей"))
                .andExpect(jsonPath("$.errors." + fieldName).value(expectedMessage));
    }
}
