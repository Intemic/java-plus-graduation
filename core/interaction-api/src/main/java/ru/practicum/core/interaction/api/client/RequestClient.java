package ru.practicum.core.interaction.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.core.interaction.api.interface_.RequestOperation;

@FeignClient(name = "request-service", path = "/inner/requests")
public interface RequestClient extends RequestOperation {
}
