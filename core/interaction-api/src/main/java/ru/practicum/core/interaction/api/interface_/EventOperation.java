package ru.practicum.core.interaction.api.interface_;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.core.interaction.api.dto.event.EventFullDto;

import java.util.List;
import java.util.Optional;

public interface EventOperation {
    @GetMapping("/{eventId}")
    Optional<EventFullDto> findById(@PathVariable @Positive Long eventId);

    @GetMapping
    List<EventFullDto> findAllByIdIn(@RequestParam(required = false) List<Long> eventIds);

    @GetMapping("/{eventId}/exist")
    boolean existsByCategoryId(@PathVariable @Positive Long eventId);
}
