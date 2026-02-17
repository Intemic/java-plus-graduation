package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.interaction.api.dto.user.UserDto;
import ru.practicum.core.interaction.api.interface_.UserOperation;
import ru.practicum.user.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/inner/users")
@RequiredArgsConstructor
public class UserInnerController implements UserOperation {
    private final UserService userService;

    @Override
    public Optional<UserDto> findById(Long userId) {
        return userService.findById(userId);
    }

    @Override
    public List<UserDto> findAllByIdIn(List<Long> ids) {
        return userService.findAllByIdIn(ids);
    }

    @Override
    public boolean existsById(Long userId) {
        return userService.existsById(userId);
    }
}
