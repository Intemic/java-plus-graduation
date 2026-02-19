package ru.practicum.category.mapper;

import ru.practicum.core.interaction.api.dto.category.CategoryDto;
import ru.practicum.category.model.Category;

public class CategoryMapper {
    public static CategoryDto mapFromCategory(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category mapToCategory(CategoryDto categoryDto) {
        return Category.builder()
                .id(categoryDto.getId())
                .name(categoryDto.getName())
                .build();
    }
}
