package ru.practicum.core.interaction.api.interface_;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.core.interaction.api.enums.RequestStatus;

import java.util.Collection;
import java.util.List;


public interface RequestOperation {
    @GetMapping("/events/{eventId}")
    List<ParticipationRequestDto> findByEventId(@Positive @PathVariable Long eventId);

    @GetMapping("/events/{eventId}/status/{status}/count")
    Long countByEventIdAndStatus(@Positive @PathVariable Long eventId, @NotNull @PathVariable RequestStatus status);

    @GetMapping("/events/{eventId}/requests")
    List<ParticipationRequestDto> findByIdInAndEventId(@Positive @PathVariable Long eventId,
                                                       @RequestParam List<Long> requestIds);

    @GetMapping("/status/{status}")
    List<ParticipationRequestDto> findAllByEventIdInAndStatus(@PathVariable @NotNull RequestStatus status,
                                                              @RequestParam Collection<Long> eventIds);


    @PatchMapping("/update-status")
    void updateStatus(@RequestBody List<ParticipationRequestDto> requests);
}

