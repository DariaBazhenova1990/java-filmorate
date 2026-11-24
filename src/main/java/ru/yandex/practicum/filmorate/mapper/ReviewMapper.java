package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.ReviewDto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewDto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReviewMapper {
    public static Review mapToReview(NewReviewRequest request) {
        Review review = new Review();
        review.setContent(request.getContent());
        review.setIsPositive(request.getIsPositive());
        review.setUserId(request.getUserId());
        review.setFilmId(request.getFilmId());
        return review;
    }

    public static Review updateReviewFields(Review review, UpdateReviewRequest request) {
        review.setContent(request.getContent());
        review.setIsPositive(request.getIsPositive());
        return review;
    }

    public static ReviewDto mapToReviewDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(review.getReviewId());
        dto.setContent(review.getContent());
        dto.setIsPositive(review.getIsPositive());
        dto.setUserId(review.getUserId());
        dto.setFilmId(review.getFilmId());
        dto.setUseful(review.getUseful());
        return dto;
    }
}
