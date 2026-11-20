package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {
    public static Event mapToEvent(EventDto dto) {
        Event event = new Event();
        event.setEventId(dto.getEventId());
        event.setTimestamp(dto.getTimestamp());
        event.setUserId(dto.getUserId());
        event.setEventType(dto.getEventType());
        event.setOperation(dto.getOperation());
        event.setEntityId(dto.getEntityId());
        return event;
    }

    public static EventDto mapToEventDto(Event event) {
        EventDto dto = new EventDto();
        dto.setEventId(event.getEventId());
        dto.setTimestamp(event.getTimestamp());
        dto.setUserId(event.getUserId());
        dto.setEventType(event.getEventType());
        dto.setOperation(event.getOperation());
        dto.setEntityId(event.getEntityId());
        return dto;
    }
}