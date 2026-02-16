package ru.practicum.core.interaction.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.core.interaction.api.interface_.CategoryOperation;

@FeignClient(name = "main-service", path = "/inner/main")
public interface CategoryClient extends CategoryOperation {
}
