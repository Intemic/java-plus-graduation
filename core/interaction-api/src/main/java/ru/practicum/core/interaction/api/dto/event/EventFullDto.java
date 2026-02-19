package ru.practicum.core.interaction.api.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.practicum.core.interaction.api.dto.category.CategoryDto;
import ru.practicum.core.interaction.api.enums.EventState;
import ru.practicum.core.interaction.api.dto.user.UserShortDto;

/**
 * DTO для полного представления события.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    private String createdOn;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String eventDate;

    private Long id;
    private UserShortDto initiator;
    private LocationDto location;
    private Boolean paid;

    @JsonProperty(defaultValue = "0")
    private Integer participantLimit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String publishedOn;

    @JsonProperty(defaultValue = "true")
    private Boolean requestModeration;

    private EventState state;
    private String title;
    private Long views;
}