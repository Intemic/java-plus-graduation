package ru.practicum.core.interaction.api.interface_;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.core.interaction.api.dto.event.EventFullDto;

import java.util.Optional;

public interface EventOperation {
    @GetMapping("/{eventId}")
    Optional<EventFullDto> findById(@PathVariable @Positive Long eventId);
}
