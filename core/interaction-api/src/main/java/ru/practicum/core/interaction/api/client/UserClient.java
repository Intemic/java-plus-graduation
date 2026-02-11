package ru.practicum.core.interaction.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.core.interaction.api.interface_.UserOperation;

@FeignClient(name = "user-service", path = "/inner/users")
public interface UserClient extends UserOperation {
}
