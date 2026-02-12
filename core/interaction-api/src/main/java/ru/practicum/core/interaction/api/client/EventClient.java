package ru.practicum.core.interaction.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.core.interaction.api.fallback.UserClientFallback;
import ru.practicum.core.interaction.api.interface_.EventOperation;

@FeignClient(name = "event-service", path = "/inner/events", fallback = UserClientFallback.class)
public interface EventClient extends EventOperation {
}
