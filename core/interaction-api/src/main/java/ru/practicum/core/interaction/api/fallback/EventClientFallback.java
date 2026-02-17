package ru.practicum.core.interaction.api.fallback;

import org.springframework.stereotype.Component;
import ru.practicum.core.interaction.api.dto.event.EventFullDto;
import ru.practicum.core.interaction.api.interface_.EventOperation;

import java.util.List;
import java.util.Optional;

@Component
public class EventClientFallback implements EventOperation {
    @Override
    public Optional<EventFullDto> findById(Long eventId) {
        return Optional.empty();
    }

    @Override
    public List<EventFullDto> findAllByIdIn(List<Long> eventIds) {
        return List.of();
    }

    @Override
    public boolean existsByCategoryId(Long eventId) {
        return false;
    }
}
