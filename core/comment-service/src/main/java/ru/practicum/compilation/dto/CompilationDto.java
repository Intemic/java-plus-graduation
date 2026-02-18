package ru.practicum.compilation.dto;

import lombok.*;
import ru.practicum.core.interaction.api.dto.event.EventShortDto;

import java.util.List;

/**
 * DTO для представления подборки событий.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {
    private List<EventShortDto> events;
    private Long id;
    private Boolean pinned;
    private String title;
}