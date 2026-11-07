package ru.yandex.practicum.filmorate.repositoryTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserRepository.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserRepositoryTests {

    private final UserRepository userStorage;

    @Test
    public void testFindUserById() {
        User newUser = new User();
        newUser.setEmail("Daria.Bazhenova@mail.ru");
        newUser.setLogin("DariaBazhenova");
        newUser.setName("Дарья");
        newUser.setBirthday(java.time.LocalDate.of(1990, 10, 5));

        User savedUser = userStorage.create(newUser);
        long generatedId = savedUser.getId();

        Optional<User> userOptional = userStorage.findById(generatedId);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", generatedId)
                );
    }

    @Test
    public void testCreateUser() {
        User newUser = new User();
        newUser.setEmail("new.user@mail.ru");
        newUser.setLogin("newUser");
        newUser.setName("New User");
        newUser.setBirthday(java.time.LocalDate.of(2000, 1, 1));

        User savedUser = userStorage.create(newUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isGreaterThan(0);
        assertThat(savedUser.getEmail()).isEqualTo("new.user@mail.ru");
        assertThat(savedUser.getLogin()).isEqualTo("newUser");
        assertThat(savedUser.getName()).isEqualTo("New User");
        assertThat(savedUser.getBirthday()).isEqualTo(java.time.LocalDate.of(2000, 1, 1));
    }

    @Test
    public void testFindAll() {
        User user1 = new User();
        user1.setEmail("user1@mail.ru");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(java.time.LocalDate.of(1990, 1, 1));
        userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(java.time.LocalDate.of(1990, 1, 2));
        userStorage.create(user2);

        List<User> users = userStorage.findAll();

        assertThat(users.size()).isEqualTo(2);
    }

    @Test
    public void testUpdate() {
        User user = new User();
        user.setEmail("old@mail.ru");
        user.setLogin("oldLogin");
        user.setName("Old Name");
        user.setBirthday(java.time.LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.create(user);
        long id = savedUser.getId();

        User updatedUser = new User();
        updatedUser.setId(id);
        updatedUser.setEmail("new@mail.ru");
        updatedUser.setLogin("newLogin");
        updatedUser.setName("New Name");
        updatedUser.setBirthday(java.time.LocalDate.of(1990, 1, 2));

        User result = userStorage.update(updatedUser);

        assertThat(result).hasFieldOrPropertyWithValue("id", id);
        assertThat(result).hasFieldOrPropertyWithValue("email", "new@mail.ru");
        assertThat(result).hasFieldOrPropertyWithValue("login", "newLogin");
        assertThat(result).hasFieldOrPropertyWithValue("name", "New Name");
        assertThat(result).hasFieldOrPropertyWithValue(
                "birthday", java.time.LocalDate.of(1990, 1, 2)
        );
    }

}
