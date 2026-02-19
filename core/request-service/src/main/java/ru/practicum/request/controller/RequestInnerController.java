package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.core.interaction.api.enums.RequestStatus;
import ru.practicum.core.interaction.api.interface_.RequestOperation;
import ru.practicum.request.service.RequestService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/inner/requests")
@RequiredArgsConstructor
public class RequestInnerController implements RequestOperation {
    private final RequestService requestService;

    @Override
    public List<ParticipationRequestDto> findByEventId(Long eventId) {
        return requestService.findByEventId(eventId);
    }

    @Override
    public Long countByEventIdAndStatus(Long eventId, RequestStatus status) {
        return requestService.countByEventIdAndStatus(eventId, status);
    }

    @Override
    public List<ParticipationRequestDto> findByIdInAndEventId(Long eventId, List<Long> requestIds) {
        return requestService.findByIdInAndEventId(eventId, requestIds);
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventIdInAndStatus(RequestStatus status, Collection<Long> eventIds) {
        return requestService.findAllByEventIdInAndStatus(status, eventIds);
    }

    @Override
    public void updateStatus(List<ParticipationRequestDto> requests) {
        requestService.updateStatus(requests);
    }
}
