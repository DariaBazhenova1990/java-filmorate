package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.ReviewsRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.ReviewDto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewDto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewsService {
    private final ReviewsRepository reviewsRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

    public ReviewDto create(NewReviewRequest newReview) {
        log.info("Получен запрос на добавление нового отзыва к фильму с ID: {}", newReview.getFilmId());

        checkAndGetUser(newReview.getUserId());
        checkAndGetFilm(newReview.getFilmId());
        Review review = ReviewMapper.mapToReview(newReview);

        review = reviewsRepository.create(review);
        log.info("Отзыв успешно добавлен. Присвоен ID: {}", review.getReviewId());

        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDto getReviewById(long id) {
        Review review = checkAndGetReview(id);
        return ReviewMapper.mapToReviewDto(review);
    }

    public void delete(long id) {
        log.info("Получен запрос на удаление отзыва с id = {}", id);

        if (id <= 0) {
            log.warn("Ошибка валидации при удалении: id должен быть указан");
            throw new ValidationException("Id для удаления должен быть указан");
        }

        boolean isDeleted = reviewsRepository.delete(id);

        if (!isDeleted) {
            log.warn("Ошибка удаления: отзыв с id = {} не найден", id);
            throw new NotFoundException("Отзыв с id = " + id + " не найден");
        }

        log.info("Отзыв с id = {} успешно удален", id);
    }

    public ReviewDto update(UpdateReviewRequest updateRequest) {
        long reviewId = updateRequest.getReviewId();
        log.info("Получен запрос на обновление отзыва с ID: {}", reviewId);

        Review review = checkAndGetReview(reviewId);

        review = ReviewMapper.updateReviewFields(review, updateRequest);
        review = reviewsRepository.update(review);

        log.info("Отзыв с ID: {} успешно обновлен", review.getReviewId());
        return ReviewMapper.mapToReviewDto(review);

    }

    public List<ReviewDto> getReviews(Long filmId, int count) {
        log.info("Получение отзывов с критериями: filmId={}, count={}", filmId, count);
        return reviewsRepository.getReviews(filmId, count)
                .stream()
                .map(ReviewMapper::mapToReviewDto)
                .toList();
    }

    public ReviewDto addLike(long id, long userId) {
        log.info("Получен запрос на лайк отзыва с ID: {}", id);
        checkAndGetReview(id);
        checkAndGetUser(userId);

        Review likedReview = reviewsRepository.addLike(id, userId);
        log.info("Пользователь с ID: {} успешно лайкнул отзыв с ID {}", userId, id);

        return ReviewMapper.mapToReviewDto(likedReview);
    }

    public ReviewDto addDislike(long id, long userId) {
        log.info("Получен запрос на дизлайк отзыва с ID: {}", id);
        checkAndGetReview(id);
        checkAndGetUser(userId);

        Review dislikedReview = reviewsRepository.addDislike(id, userId);
        log.info("Пользователь с ID: {} успешно дизлайкнул отзыв с ID {}", userId, id);

        return ReviewMapper.mapToReviewDto(dislikedReview);
    }

    public ReviewDto removeLike(long id, long userId) {
        log.info("Получен запрос на отмену лайка отзыва с ID: {}", id);
        checkAndGetReview(id);
        checkAndGetUser(userId);

        Review review = reviewsRepository.removeLike(id, userId);
        log.info("Пользователь с ID: {} успешно отменил лайк отзыва с ID {}", userId, id);

        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDto removeDislike(long id, long userId) {
        log.info("Получен запрос на отмену дизлайка отзыва с ID: {}", id);
        checkAndGetReview(id);
        checkAndGetUser(userId);

        Review review = reviewsRepository.removeDisLike(id, userId);
        log.info("Пользователь с ID: {} успешно отменил дизлайк отзыва с ID {}", userId, id);

        return ReviewMapper.mapToReviewDto(review);
    }

    private Review checkAndGetReview(long id) {
        return reviewsRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ошибка поиска: Отзыв с ID {} не найден", id);
                    return new NotFoundException("Отзыв с id = " + id + " не найден");
                });
    }

    private User checkAndGetUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка поиска: Пользователь с ID {} не найден", userId);
                    return new NotFoundException("Пользователь с id = " + userId + " не найден");
                });
    }

    private Film checkAndGetFilm(long id) {
        return filmRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ошибка поиска: Фильм с ID {} не найден", id);
                    return new NotFoundException("Фильм с id = " + id + " не найден");
                });
    }
}
