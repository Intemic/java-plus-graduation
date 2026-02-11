package ru.practicum.core.interaction.api.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.core.interaction.api.dto.category.CategoryDto;
import ru.practicum.core.interaction.api.dto.user.UserShortDto;

/**
 * DTO для краткого представления события.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String eventDate;

    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
}