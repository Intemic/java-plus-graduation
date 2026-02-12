package ru.practicum.core.interaction.api.fallback;

import org.springframework.stereotype.Component;
import ru.practicum.core.interaction.api.dto.user.UserDto;
import ru.practicum.core.interaction.api.interface_.UserOperation;

import java.util.Optional;

@Component
public class UserClientFallback implements UserOperation {
    @Override
    public Optional<UserDto> findById(Long userId) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long userId) {
        return false;
    }
}
