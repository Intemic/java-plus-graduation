package ru.practicum.request.service.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.core.interaction.api.enums.RequestStatus;
import ru.practicum.core.interaction.api.interface_.RequestOperation;

import java.util.Collection;
import java.util.List;


@RestController
@RequestMapping("/inner/requests")
@RequiredArgsConstructor
public class RequestInnerController implements RequestOperation {
    @Override
    public boolean test() {
        return true;
    }

    @Override
    public List<ParticipationRequestDto> findByEventId(Long eventId, List<Long> requestIds) {
        return List.of();
    }

    @Override
    public Long countByEventIdAndStatus(Long eventId, RequestStatus status) {
        return 0L;
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventIdInAndStatus(RequestStatus status, Collection<Long> eventIds) {
        return List.of();
    }

    @Override
    public void updateStatus(List<ParticipationRequestDto> requests) {

    }
}
