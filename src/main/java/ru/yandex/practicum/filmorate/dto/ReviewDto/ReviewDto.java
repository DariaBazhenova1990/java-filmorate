package ru.yandex.practicum.filmorate.dto.ReviewDto;

import lombok.Data;

@Data
public class ReviewDto {
    private long reviewId;
    private String content;
    private Boolean isPositive;
    private long userId;
    private long filmId;
    private int useful;
}
