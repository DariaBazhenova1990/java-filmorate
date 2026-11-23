package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewDto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewsService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Validated
@RequiredArgsConstructor
public class ReviewsController {

    private final ReviewsService reviewService;

    @PostMapping
    public ReviewDto create(@Valid @RequestBody NewReviewRequest newReview) {
        return reviewService.create(newReview);
    }

    @PutMapping
    public ReviewDto update(@Valid @RequestBody UpdateReviewRequest reviewUpdate) {
        return reviewService.update(reviewUpdate);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") long id) {
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public ReviewDto getReviewById(@PathVariable("id") long id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<ReviewDto> getReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") @Positive int count
    ) {
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public ReviewDto addLike(
            @PathVariable("id") long id,
            @PathVariable("userId") long userId
    ) {
        return reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ReviewDto addDislike(
            @PathVariable("id") long id,
            @PathVariable("userId") long userId
    ) {
        return reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ReviewDto removeLike(
            @PathVariable("id") long id,
            @PathVariable("userId") long userId
    ) {
        return reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ReviewDto removeDislike(
            @PathVariable("id") long id,
            @PathVariable("userId") long userId
    ) {
        return reviewService.removeDislike(id, userId);
    }
}
