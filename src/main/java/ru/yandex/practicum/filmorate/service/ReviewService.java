package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.ReviewDto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewDto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final FeedService feedService;

    public ReviewDto create(NewReviewRequest request) {
        log.info("Получен запрос на добавление отзыва. Фильм: {}, пользователь: {}",
                request.getFilmId(), request.getUserId());
        checkUserExists(request.getUserId());
        checkFilmExists(request.getFilmId());

        Review review = reviewRepository.create(ReviewMapper.mapToReview(request));
        feedService.addEvent(review.getUserId(), EventType.REVIEW, Operation.ADD, review.getReviewId());
        log.info("Отзыв успешно добавлен. Присвоен ID: {}", review.getReviewId());
        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDto update(UpdateReviewRequest request) {
        long id = request.getReviewId();
        log.info("Получен запрос на обновление отзыва с ID: {}", id);
        Review review = getReviewOrThrow(id);
        review = reviewRepository.update(ReviewMapper.updateReviewFields(review, request));
        feedService.addEvent(review.getUserId(), EventType.REVIEW, Operation.UPDATE, review.getReviewId());
        log.info("Отзыв с ID: {} успешно обновлен", id);
        return ReviewMapper.mapToReviewDto(review);
    }

    public void delete(long id) {
        log.info("Получен запрос на удаление отзыва с ID: {}", id);
        Review review = getReviewOrThrow(id);
        reviewRepository.deleteById(id);
        feedService.addEvent(review.getUserId(), EventType.REVIEW, Operation.REMOVE, review.getReviewId());
        log.info("Отзыв с ID: {} удален", id);
    }

    public ReviewDto getById(long id) {
        log.info("Получение отзыва по ID {}", id);
        return ReviewMapper.mapToReviewDto(getReviewOrThrow(id));
    }

    public List<ReviewDto> getReviews(Long filmId, int count) {
        log.info("Получение отзывов. Фильм: {}, количество: {}", filmId, count);
        List<Review> reviews;
        if (filmId == null) {
            reviews = reviewRepository.findAll(count);
        } else {
            checkFilmExists(filmId);
            reviews = reviewRepository.findByFilmId(filmId, count);
        }
        return reviews.stream().map(ReviewMapper::mapToReviewDto).toList();
    }

    @Transactional
    public ReviewDto addReaction(long id, long userId, boolean isLike) {
        log.info("Пользователь {} ставит {} отзыву {}", userId, isLike ? "лайк" : "дизлайк", id);
        getReviewOrThrow(id);
        checkUserExists(userId);
        reviewRepository.setReaction(id, userId, isLike);
        return ReviewMapper.mapToReviewDto(getReviewOrThrow(id));
    }

    @Transactional
    public ReviewDto removeReaction(long id, long userId) {
        log.info("Пользователь {} удаляет свою оценку у отзыва {}", userId, id);
        getReviewOrThrow(id);
        checkUserExists(userId);
        reviewRepository.removeReaction(id, userId);
        return ReviewMapper.mapToReviewDto(getReviewOrThrow(id));
    }

    private Review getReviewOrThrow(long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ошибка поиска: отзыв с ID {} не найден", id);
                    return new NotFoundException("Отзыв с id = " + id + " не найден");
                });
    }

    private void checkUserExists(long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("Ошибка поиска: пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private void checkFilmExists(long filmId) {
        if (filmRepository.findById(filmId).isEmpty()) {
            log.warn("Ошибка поиска: фильм с ID {} не найден", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
    }
}
