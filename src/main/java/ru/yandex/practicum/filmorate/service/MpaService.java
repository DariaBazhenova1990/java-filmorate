package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MpaService {
    private final MpaRepository mpaRepository;

    public MpaService(MpaRepository mpaRepository) {
        this.mpaRepository = mpaRepository;
    }

    public List<MpaDto> getAllMpa() {
        log.info("Получение списка всех рейтингов");
        return mpaRepository.getAllMpa()
                .stream()
                .map(MpaMapper::mapToMpaDto)
                .collect(Collectors.toList());
    }

    public MpaDto getMpaById(int id) {
        Mpa mpa = mpaRepository.getMpaById(id)
                .orElseThrow(() -> {
                            log.warn("Ошибка поиска: MPA с ID {} отсутствует", id);
                            return new NotFoundException("MPA с id = " + id + " отсутствует");
                        }
                );
        return MpaMapper.mapToMpaDto(mpa);
    }
}
