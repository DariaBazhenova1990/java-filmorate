package ru.yandex.practicum.filmorate.dto.ReviewDto;

import lombok.Data;

@Data
public class ReviewDto {
    private long reviewId;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private int useful;

}
