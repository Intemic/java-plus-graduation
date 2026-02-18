package ru.practicum.category.service;

import ru.practicum.core.interaction.api.dto.category.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с категориями.
 */
public interface CategoryService {

    /**
     * Возвращает список категорий с пагинацией.
     *
     * @param from начальная позиция
     * @param size количество элементов
     * @return список категорий
     */
    List<CategoryDto> getAll(int from, int size);

    /**
     * Возвращает категорию по идентификатору.
     *
     * @param catId идентификатор категории
     * @return данные категории
     */
    CategoryDto get(long catId);

    /**
     * Возвращает сущность категории по идентификатору.
     *
     * @param catId идентификатор категории
     * @return сущность категории
     */
    Category getCategoryById(long catId);

    /**
     * Создает новую категорию.
     *
     * @param categoryDto данные для создания
     * @return созданная категория
     */
    CategoryDto create(NewCategoryDto categoryDto);

    /**
     * Обновляет существующую категорию.
     *
     * @param categoryDto данные для обновления
     * @return обновленная категория
     */
    CategoryDto update(CategoryDto categoryDto);

    /**
     * Удаляет категорию по идентификатору.
     *
     * @param catId идентификатор категории
     */
    void delete(long catId);

    Optional<CategoryDto> findByCategoryId(Long categoryId);

    List<CategoryDto> findAllByIdIn(List<Long> categoryIds);
}