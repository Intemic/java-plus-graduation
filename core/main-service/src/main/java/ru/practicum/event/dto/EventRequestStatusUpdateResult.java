package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.core.interaction.api.dto.request.ParticipationRequestDto;

import java.util.List;

/**
 * DTO для результата обновления статусов запросов.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDto> rejectedRequests;
}