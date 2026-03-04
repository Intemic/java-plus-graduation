package ru.practicum.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.core.interaction.api.dto.event.EventFullDto;
import ru.practicum.core.interaction.api.dto.event.EventShortDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public static CompilationDto toDto(Compilation compilation,
                                       Map<Long, EventFullDto> eventDtoMap) {
        if (compilation == null) {
            return null;
        }

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(mapToEventShortDto(compilation.getEvents(), eventDtoMap))
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

    public static List<EventShortDto> mapToEventShortDto(Set<Long> eventIds,
                                                         Map<Long, EventFullDto> eventDtoMap) {
        if (eventDtoMap == null)
            return List.of();

        return eventIds.stream()
                .map(eventId -> {
                    EventFullDto event = eventDtoMap.get(eventId);

                    return EventShortDto.builder()
                        .id(event.getId())
                        .title(event.getTitle())
                        .annotation(event.getAnnotation())
                        .category(event.getCategory())
                        .initiator(event.getInitiator())
                        .paid(event.getPaid())
                        .eventDate(event.getEventDate())
                        .confirmedRequests(event.getConfirmedRequests())
                        .rating(event.getRating())
                        .build(); })
                .toList();

    }
}