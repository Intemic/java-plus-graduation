package ru.practicum.core.interaction.api.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO для представления категории.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    @Positive
    private Long id;

    @Size(min = 1, max = 50)
    @NotBlank
    private String name;
}