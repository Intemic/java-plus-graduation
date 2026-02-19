package ru.practicum.core.interaction.api.fallback;

import org.springframework.stereotype.Component;
import ru.practicum.core.interaction.api.dto.user.UserDto;
import ru.practicum.core.interaction.api.interface_.UserOperation;

import java.util.List;
import java.util.Optional;

@Component
public class UserClientFallback implements UserOperation {
    @Override
    public Optional<UserDto> findById(Long userId) {
        return Optional.empty();
    }

    @Override
    public List<UserDto> findAllByIdIn(List<Long> ids) {
        return List.of();
    }

    @Override
    public boolean existsById(Long userId) {
        return false;
    }
}
