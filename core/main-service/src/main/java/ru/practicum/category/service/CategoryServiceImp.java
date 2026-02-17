package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.interaction.api.dto.category.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.core.interaction.api.client.EventClient;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы с категориями.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImp implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventClient eventRepository;

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::mapFromCategory)
                .toList();
    }

    @Override
    public CategoryDto get(long catId) {
        Category category = getCategoryById(catId);
        return CategoryMapper.mapFromCategory(category);
    }

    @Override
    public Category getCategoryById(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundResource("Категория с id=" + catId + " не найдена"));
    }

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto categoryDto) {
        categoryRepository.findByNameContainingIgnoreCase(categoryDto.getName())
                .ifPresent(category -> {
                    throw new ConflictResource("Категория '" + categoryDto.getName() + "' уже существует");
                });

        Category category = categoryDto.mapToCategory();
        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.mapFromCategory(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto) {
        Category existingCategory = getCategoryById(categoryDto.getId());

        categoryRepository.findByNameContainingIgnoreCaseAndIdNotIn(categoryDto.getName(),
                        List.of(categoryDto.getId()))
                .ifPresent(category -> {
                    throw new ConflictResource("Категория '" + categoryDto.getName() + "' уже существует");
                });

        existingCategory.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(existingCategory);

        return CategoryMapper.mapFromCategory(updatedCategory);
    }

    @Override
    @Transactional
    public void delete(long catId) {
        Category category = getCategoryById(catId);

        boolean hasEvents = eventRepository.existsByCategoryId(catId);
        if (hasEvents) {
            throw new ConflictResource("Нельзя удалить категорию: существуют события, связанные с этой категорией");
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    public Optional<CategoryDto> findByCategoryId(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(category ->  CategoryMapper.mapFromCategory(category));
    }

    @Override
    public List<CategoryDto> findAllByIdIn(List<Long> categoryIds) {
        return categoryRepository.findAllById(categoryIds).stream()
                .map(CategoryMapper::mapFromCategory)
                .toList();
    }
}