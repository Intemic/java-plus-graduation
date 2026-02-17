package ru.practicum.core.interaction.api.interface_;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.core.interaction.api.dto.category.CategoryDto;

import java.util.List;
import java.util.Optional;

public interface CategoryOperation {
    @GetMapping("/{categoryId}")
    Optional<CategoryDto> findByCategoryId(@PathVariable @Positive Long categoryId);

    @GetMapping
    List<CategoryDto> findAllByIdIn(@RequestParam List<Long> categoryIds);
}
