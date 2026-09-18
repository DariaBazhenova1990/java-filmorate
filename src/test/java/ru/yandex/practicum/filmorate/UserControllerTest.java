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
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserController userController;

    private static Stream<Arguments> provideInvalidUsers() {
        User invalidEmail = createValidUser();
        invalidEmail.setEmail("invalid-email.ru");

        User spaceInLogin = createValidUser();
        spaceInLogin.setLogin("invalid login");

        User blankLogin = createValidUser();
        blankLogin.setLogin(null);

        User futureBirthday = createValidUser();
        futureBirthday.setBirthday(LocalDate.now().plusDays(1));

        return Stream.of(
                Arguments.of(invalidEmail, "email",
                        "Электронная почта должна содержать символ @ и соответствовать формату"),
                Arguments.of(spaceInLogin, "login", "Логин не может содержать пробелы"),
                Arguments.of(blankLogin, "login", "Логин не может быть пустым"),
                Arguments.of(futureBirthday, "birthday", "Дата рождения не может быть в будущем")
        );
    }

    private static User createValidUser() {
        User user = new User();
        user.setEmail("Daria.Bazhenova@mail.ru");
        user.setLogin("DariaBazhenova");
        user.setName("Дарья");
        user.setBirthday(LocalDate.of(1990, 10, 5));
        return user;
    }

    @BeforeEach
    void setUp() {
        userController.deleteAllUsers();
    }

    @Test
    void createUserWithValidData() throws Exception {
        User user = createValidUser();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createUserWithEmptyName() throws Exception {
        User user = createValidUser();
        user.setName("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("DariaBazhenova"));
    }

    @ParameterizedTest(name = "{index} ==> Проверка поля ''{1}'' на ошибку: ''{2}''")
    @MethodSource("provideInvalidUsers")
    void failUserCreationWithInvalidData(User invalidUser, String fieldName, String expectedMessage) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации полей"))
                .andExpect(jsonPath("$.errors." + fieldName).value(expectedMessage));
    }
}
