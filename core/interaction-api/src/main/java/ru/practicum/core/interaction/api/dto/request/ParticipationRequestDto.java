package ru.practicum.core.interaction.api.dto.request;

import lombok.*;
import ru.practicum.core.interaction.api.enums.RequestStatus;

/**
 * DTO для представления заявки на участие в событии.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    private String created;
    private Long event;
    private Long id;
    private Long requester;
    private RequestStatus status;
}