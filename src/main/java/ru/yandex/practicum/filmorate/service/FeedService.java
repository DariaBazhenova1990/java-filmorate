package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    public List<EventDto> getFeed(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        log.debug("Получение ленты событий для пользователя с id = {}", userId);
        return feedRepository.findByUserId(userId)
                .stream()
                .map(EventMapper::mapToEventDto)
                .collect(Collectors.toList());
    }

    public void addEvent(long userId, EventType eventType, Operation operation, long entityId) {
        Event event = new Event();
        event.setTimestamp(Instant.now().toEpochMilli());
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setOperation(operation);
        event.setEntityId(entityId);

        Event created = feedRepository.create(event);
        log.debug("Сохранено событие: {}", created);
    }
}