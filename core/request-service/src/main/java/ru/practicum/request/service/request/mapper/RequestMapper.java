package ru.practicum.request.service.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.core.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.request.service.request.model.Request;

import java.time.format.DateTimeFormatter;

/**
 * Маппер для преобразования между сущностями и DTO заявок.
 */
@UtilityClass
public class RequestMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Преобразует сущность в DTO.
     *
     * @param request сущность заявки
     * @return DTO заявки
     */
    public static ParticipationRequestDto mapToParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated().format(FORMATTER))
                //.event(request.getEvent().getId())
                .event(request.getEvent())
                //.requester(request.getRequester().getId())
                .requester(request.getRequester())
                .status(request.getStatus())
                .build();
    }
}