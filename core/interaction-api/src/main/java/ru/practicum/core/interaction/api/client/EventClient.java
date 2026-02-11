package ru.practicum.core.interaction.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.core.interaction.api.interface_.EventOperation;

@FeignClient(name = "event-service", path = "/inner/events")
public interface EventClient extends EventOperation {
}
