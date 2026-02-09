package ru.practicum.core.interaction.api.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "request-service")
public interface RequestClient {
}
