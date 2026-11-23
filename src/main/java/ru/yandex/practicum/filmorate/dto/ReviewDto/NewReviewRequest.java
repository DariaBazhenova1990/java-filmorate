package ru.yandex.practicum.filmorate.dto.ReviewDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewReviewRequest {
    @NotBlank(message = "Текст отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва (isPositive) должен быть указан")
    private Boolean isPositive;

    @NotNull(message = "Идентификатор пользователя должен быть указан")
    private Long userId;

    @NotNull(message = "Идентификатор фильма должен быть указан")
    private Long filmId;
}
