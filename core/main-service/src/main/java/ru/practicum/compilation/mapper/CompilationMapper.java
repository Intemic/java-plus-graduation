package ru.practicum.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.core.interaction.api.client.UserClient;
import ru.practicum.event.mapper.EventMapper;

import java.util.stream.Collectors;

/**
 * Маппер для преобразования между сущностями и DTO подборок событий.
 */
@UtilityClass
public class CompilationMapper {

    /**
     * Преобразует сущность в DTO.
     *
     * @param compilation сущность подборки
     * @return DTO подборки
     */
    public static CompilationDto toDto(Compilation compilation, UserClient userRepository) {
        if (compilation == null) {
            return null;
        }

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(compilation.getEvents() != null ?
                        compilation.getEvents().stream()
                                .map(event ->  EventMapper.mapToEventShortDto(event, userRepository))
                                .collect(Collectors.toList()) :
                        java.util.Collections.emptyList())
                .build();
    }

    /**
     * Преобразует DTO в сущность.
     *
     * @param newCompilationDto DTO для создания
     * @return сущность подборки
     */
    public static Compilation toEntity(NewCompilationDto newCompilationDto) {
        if (newCompilationDto == null) {
            return null;
        }

        return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false)
                .build();
    }
}