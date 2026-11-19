package ru.yandex.practicum.filmorate.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.SearchBy;

@Component
@Slf4j
public class StringToSearchByConverter implements Converter<String, SearchBy> {

    @Override
    public SearchBy convert(String source) {
        log.info("Конвертируем параметр: '{}'", source);
        return SearchBy.valueOf(source.trim().toUpperCase());
    }
}
