package ru.yandex.practicum.filmorate.repositoryTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({ReviewRepository.class, ReviewRowMapper.class, FilmRepository.class, FilmRowMapper.class,
        GenreRowMapper.class, UserRepository.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ReviewRepositoryTests {

    private final ReviewRepository reviewRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    private User createUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.ru");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userRepository.create(user);
    }

    private Film createFilm(String name) {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        film.setMpa(mpa);
        return filmRepository.create(film);
    }

    private Review newReview(long userId, long filmId, String content, boolean positive) {
        Review review = new Review();
        review.setContent(content);
        review.setIsPositive(positive);
        review.setUserId(userId);
        review.setFilmId(filmId);
        return reviewRepository.create(review);
    }

    @Test
    public void testCreateAndFindReview() {
        User user = createUser("reviewer1");
        Film film = createFilm("Film A");

        Review saved = newReview(user.getId(), film.getId(), "Great film", true);

        assertThat(saved.getReviewId()).isPositive();
        assertThat(saved.getContent()).isEqualTo("Great film");
        assertThat(saved.getIsPositive()).isTrue();
        assertThat(saved.getUseful()).isZero();
        assertThat(reviewRepository.findById(saved.getReviewId())).isPresent();
    }

    @Test
    public void testUpdateReview() {
        User user = createUser("reviewer2");
        Film film = createFilm("Film B");
        Review saved = newReview(user.getId(), film.getId(), "Old", true);

        saved.setContent("New");
        saved.setIsPositive(false);
        Review updated = reviewRepository.update(saved);

        assertThat(updated.getContent()).isEqualTo("New");
        assertThat(updated.getIsPositive()).isFalse();
        assertThat(updated.getUserId()).isEqualTo(user.getId());
        assertThat(updated.getFilmId()).isEqualTo(film.getId());
    }

    @Test
    public void testDeleteReview() {
        User user = createUser("reviewer3");
        Film film = createFilm("Film C");
        Review saved = newReview(user.getId(), film.getId(), "To delete", true);

        assertThat(reviewRepository.deleteById(saved.getReviewId())).isTrue();
        assertThat(reviewRepository.findById(saved.getReviewId())).isEmpty();
    }

    @Test
    public void testUsefulCalculationAndOrdering() {
        User author = createUser("author");
        User u1 = createUser("fan1");
        User u2 = createUser("fan2");
        Film film = createFilm("Film D");

        Review r1 = newReview(author.getId(), film.getId(), "First", true);
        Review r2 = newReview(author.getId(), film.getId(), "Second", false);

        reviewRepository.setReaction(r2.getReviewId(), u1.getId(), true);
        reviewRepository.setReaction(r2.getReviewId(), u2.getId(), true);
        reviewRepository.setReaction(r1.getReviewId(), u1.getId(), false);

        assertThat(reviewRepository.findById(r2.getReviewId()).orElseThrow().getUseful()).isEqualTo(2);
        assertThat(reviewRepository.findById(r1.getReviewId()).orElseThrow().getUseful()).isEqualTo(-1);

        List<Review> byFilm = reviewRepository.findByFilmId(film.getId(), 10);
        assertThat(byFilm).extracting(Review::getReviewId)
                .containsExactly(r2.getReviewId(), r1.getReviewId());

        // смена дизлайка на лайк пересчитывает рейтинг: -1 -> +1
        reviewRepository.setReaction(r1.getReviewId(), u1.getId(), true);
        assertThat(reviewRepository.findById(r1.getReviewId()).orElseThrow().getUseful()).isEqualTo(1);

        // удаление лайка возвращает рейтинг к нулю
        reviewRepository.removeReaction(r1.getReviewId(), u1.getId());
        assertThat(reviewRepository.findById(r1.getReviewId()).orElseThrow().getUseful()).isZero();
    }

    @Test
    public void testFindByFilmRespectsCount() {
        User user = createUser("reviewer4");
        Film film = createFilm("Film E");
        newReview(user.getId(), film.getId(), "1", true);
        newReview(user.getId(), film.getId(), "2", true);
        newReview(user.getId(), film.getId(), "3", true);

        assertThat(reviewRepository.findByFilmId(film.getId(), 2)).hasSize(2);
    }
}
