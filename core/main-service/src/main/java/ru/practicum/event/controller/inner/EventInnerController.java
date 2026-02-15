package ru.practicum.event.controller.inner;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.interaction.api.dto.event.EventFullDto;
import ru.practicum.core.interaction.api.interface_.EventOperation;
import ru.practicum.event.service.EventService;

import java.util.Optional;

@RestController
@RequestMapping("/inner/events")
@RequiredArgsConstructor
public class EventInnerController implements EventOperation {
    private final EventService eventService;

    @Override
    public Optional<EventFullDto> findById(Long eventId) {
        return eventService.findById(eventId);
    }
}
