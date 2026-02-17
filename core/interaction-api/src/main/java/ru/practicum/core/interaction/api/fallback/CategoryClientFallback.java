package ru.practicum.core.interaction.api.fallback;

import org.springframework.stereotype.Component;
import ru.practicum.core.interaction.api.dto.category.CategoryDto;
import ru.practicum.core.interaction.api.interface_.CategoryOperation;

import java.util.List;
import java.util.Optional;

@Component
public class CategoryClientFallback implements CategoryOperation {
    @Override
    public Optional<CategoryDto> findByCategoryId(Long categoryId) {
        return Optional.empty();
    }

    @Override
    public List<CategoryDto> findAllByIdIn(List<Long> categoryIds) {
        return List.of();
    }
}
