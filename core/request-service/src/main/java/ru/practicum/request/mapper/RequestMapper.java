package ru.practicum.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.core.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.request.model.Request;

import java.time.LocalDateTime;
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
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus())
                .build();
    }

    public static Request mapFromParticipationRequestDto(ParticipationRequestDto requestDto) {
        return Request.builder()
                .id(requestDto.getId())
                .created(LocalDateTime.parse(requestDto.getCreated(), FORMATTER))
                .eventId(requestDto.getEvent())
                .requesterId(requestDto.getRequester())
                .status(requestDto.getStatus())
                .build();

    }
}