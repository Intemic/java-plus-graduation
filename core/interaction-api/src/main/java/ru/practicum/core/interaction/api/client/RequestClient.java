package ru.practicum.core.interaction.api.client;

import org.springframework.cloud.openfeign.FeignClient;
//import ru.practicum.core.interaction.api.config.FeignClientConfig;
import ru.practicum.core.interaction.api.fallback.RequestClientFallback;
import ru.practicum.core.interaction.api.interface_.RequestOperation;

//@FeignClient(name = "request-service", path = "/inner/requests", configuration = FeignClientConfig.class)
@FeignClient(name = "request-service", path = "/inner/requests", fallback = RequestClientFallback.class)
public interface RequestClient extends RequestOperation {
}
