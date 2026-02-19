package ru.practicum.core.interaction.api.interface_;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.core.interaction.api.dto.user.UserDto;

import java.util.List;
import java.util.Optional;


public interface UserOperation {
    @GetMapping("/{userId}")
    Optional<UserDto> findById(@PathVariable @Positive Long userId);

    @GetMapping
    List<UserDto> findAllByIdIn(@RequestParam List<Long> ids);

    @GetMapping("/{userId}/exists")
    boolean existsById(@PathVariable @Positive Long userId);
}
