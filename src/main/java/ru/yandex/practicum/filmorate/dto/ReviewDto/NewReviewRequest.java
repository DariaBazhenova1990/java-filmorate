package ru.yandex.practicum.filmorate.dto.ReviewDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Valid
public class NewReviewRequest {

    @NotBlank(message = "Контент не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва должен быть указан")
    private Boolean isPositive;

    @NotNull(message = "Пользователь должен быть указан")
    private Long userId;

    @NotNull(message = "Фильм должен быть указан")
    private Long filmId;

}
