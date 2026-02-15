package ru.practicum.core.interaction.api.fallback;

import org.springframework.stereotype.Component;
import ru.practicum.core.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.core.interaction.api.enums.RequestStatus;
import ru.practicum.core.interaction.api.interface_.RequestOperation;

import java.util.Collection;
import java.util.List;

@Component
public class RequestClientFallback implements RequestOperation {
    @Override
    public List<ParticipationRequestDto> findByEventId(Long eventId) {
        return List.of();
    }

    @Override
    public Long countByEventIdAndStatus(Long eventId, RequestStatus status) {
        return 0L;
    }

    @Override
    public List<ParticipationRequestDto> findByIdInAndEventId(Long eventId, List<Long> requestIds) {
        return List.of();
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventIdInAndStatus(RequestStatus status, Collection<Long> eventIds) {
        return List.of();
    }

    @Override
    public void updateStatus(List<ParticipationRequestDto> requests) {
    }
}
