package ru.practicum.core.interaction.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.core.interaction.api.fallback.CategoryClientFallback;
import ru.practicum.core.interaction.api.interface_.CategoryOperation;

@FeignClient(name = "comment-service", path = "/inner/category", fallback = CategoryClientFallback.class)
public interface CategoryClient extends CategoryOperation {
}
