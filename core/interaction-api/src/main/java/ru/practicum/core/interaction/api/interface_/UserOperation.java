package ru.practicum.core.interaction.api.interface_;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.core.interaction.api.dto.user.UserDto;

import java.util.Optional;


public interface UserOperation {
    @GetMapping("/{userId}")
    Optional<UserDto> findById(@PathVariable @Positive Long userId);

    @GetMapping("/{userId}/exists")
    boolean existsById(@PathVariable @Positive Long userId);
}
