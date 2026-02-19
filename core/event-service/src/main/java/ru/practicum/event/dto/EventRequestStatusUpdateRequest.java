package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.core.interaction.api.enums.RequestStatus;

import java.util.List;

/**
 * DTO для обновления статусов запросов на участие.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private RequestStatus status;
}