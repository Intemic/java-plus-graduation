package ru.practicum.core.interaction.api.interface_;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.core.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.core.interaction.api.enums.RequestStatus;

import java.util.List;


public interface RequestOperation {
    @GetMapping("/events/{eventId}/requests")
    List<ParticipationRequestDto> findByEventId(@Positive @PathVariable Long eventId);

    @GetMapping("/events/{eventId}/status/{status}/count")
    Long countByEventIdAndStatus(@Positive @PathVariable Long eventId, @NotNull RequestStatus status);

    @GetMapping("/events/{eventId}/requests")
    List<ParticipationRequestDto> findByIdInAndEventId(@Positive @PathVariable Long eventId,
                                                       @RequestParam List<Long> requestIds);

}
