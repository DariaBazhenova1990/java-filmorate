package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

@Data
public class EventDto {
    private long eventId;
    private long timestamp;
    private long userId;
    private EventType eventType;
    private Operation operation;
    private long entityId;
}