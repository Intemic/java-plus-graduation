package ru.practicum.category.controller.inner;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.service.CategoryService;
import ru.practicum.core.interaction.api.dto.category.CategoryDto;
import ru.practicum.core.interaction.api.interface_.CategoryOperation;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/inner/category")
@RequiredArgsConstructor
public class CategoryInnerController implements CategoryOperation {
    private final CategoryService categoryService;

    @Override
    public Optional<CategoryDto> findByCategoryId(Long categoryId) {
        return categoryService.findByCategoryId(categoryId);
    }

    @Override
    public List<CategoryDto> findAllByIdIn(List<Long> categoryIds) {
        return categoryService.findAllByIdIn(categoryIds);
    }
}
