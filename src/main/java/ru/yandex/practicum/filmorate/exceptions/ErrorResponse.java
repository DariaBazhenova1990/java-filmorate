package ru.yandex.practicum.filmorate.exceptions;

import lombok.Getter;

import java.util.Map;

@Getter
public class ErrorResponse {
    private final String error;
    private final String message;
    private Map<String, String> errors;

    public ErrorResponse(String message) {
        this.error = message;
        this.message = message;
    }

    public ErrorResponse(String message, Map<String, String> errors) {
        this.error = message;
        this.message = message;
        this.errors = errors;
    }
}