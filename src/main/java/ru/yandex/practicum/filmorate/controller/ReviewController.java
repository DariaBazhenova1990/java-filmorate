package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewDto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Validated
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ReviewDto create(@Valid @RequestBody NewReviewRequest request) {
        return reviewService.create(request);
    }

    @PutMapping
    public ReviewDto update(@Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.update(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") long id) {
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public ReviewDto getById(@PathVariable("id") long id) {
        return reviewService.getById(id);
    }

    @GetMapping
    public List<ReviewDto> getReviews(@RequestParam(required = false) Long filmId,
                                      @RequestParam(defaultValue = "10") @Positive int count) {
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public ReviewDto like(@PathVariable("id") long id, @PathVariable("userId") long userId) {
        return reviewService.addReaction(id, userId, true);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ReviewDto dislike(@PathVariable("id") long id, @PathVariable("userId") long userId) {
        return reviewService.addReaction(id, userId, false);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ReviewDto removeLike(@PathVariable("id") long id, @PathVariable("userId") long userId) {
        return reviewService.removeReaction(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ReviewDto removeDislike(@PathVariable("id") long id, @PathVariable("userId") long userId) {
        return reviewService.removeReaction(id, userId);
    }
}
