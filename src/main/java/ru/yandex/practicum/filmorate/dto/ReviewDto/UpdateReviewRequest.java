package ru.yandex.practicum.filmorate.dto.ReviewDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateReviewRequest {
    private long reviewId;

    @NotBlank(message = "Контент не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва должен быть указан")
    private Boolean isPositive;

    @NotNull(message = "Пользователь должен быть указан")
    private Long userId;

    @NotNull(message = "Пользователь должен быть указан")
    private Long filmId;

    public boolean hasContent() {
        return content != null && !content.isBlank();
    }

    public boolean hasPositive() {
        return isPositive != null;
    }

    public boolean hasUserId() {
        return userId != null;
    }

    public boolean hasFilmId() {
        return filmId != null;
    }
}
