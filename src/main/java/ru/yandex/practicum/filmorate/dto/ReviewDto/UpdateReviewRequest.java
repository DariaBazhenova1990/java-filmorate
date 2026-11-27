package ru.yandex.practicum.filmorate.dto.ReviewDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateReviewRequest {
    @NotNull(message = "Идентификатор отзыва должен быть указан")
    private Long reviewId;

    @NotBlank(message = "Текст отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва (isPositive) должен быть указан")
    private Boolean isPositive;

    // userId и filmId при редактировании игнорируются: автор и фильм отзыва не меняются
    private Long userId;
    private Long filmId;
}
