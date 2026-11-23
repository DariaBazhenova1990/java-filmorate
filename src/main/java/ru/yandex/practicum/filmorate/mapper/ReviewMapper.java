package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.ReviewDto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewDto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewMapper {

    public static Review mapToReview(NewReviewRequest reviewRequest) {
        Review review = new Review();

        review.setContent(reviewRequest.getContent());
        review.setIsPositive(reviewRequest.getIsPositive());
        review.setUserId(reviewRequest.getUserId());
        review.setFilmId(reviewRequest.getFilmId());

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

    public static Review updateReviewFields(Review review, UpdateReviewRequest reviewUpdate) {
        if (reviewUpdate.hasContent()) review.setContent(reviewUpdate.getContent());
        if (reviewUpdate.hasPositive()) review.setIsPositive(reviewUpdate.getIsPositive());
        if (reviewUpdate.hasUserId()) review.setUserId(reviewUpdate.getUserId());
        if (reviewUpdate.hasFilmId()) review.setFilmId(reviewUpdate.getFilmId());

        return review;
    }

}
