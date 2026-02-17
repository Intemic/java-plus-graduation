package ru.practicum.comment.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Сущность комментария.
 * Представляет комментарий пользователя к событию в системе.
 * Содержит информацию об авторе, событии, тексте комментария и времени создания.
 */
@Builder(toBuilder = true)
@Table(name = "comments")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    /**
     * Уникальный идентификатор комментария.
     * Генерируется автоматически при сохранении в базу данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Автор комментария.
     * Связь многие-к-одному с сущностью User.
     * Загружается лениво для оптимизации производительности.
     */
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    /**
     * Событие, к которому относится комментарий.
     * Связь многие-к-одному с сущностью Event.
     * Загружается лениво для оптимизации производительности.
     */
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /**
     * Дата и время создания комментария.
     * Устанавливается автоматически при создании комментария.
     */
    @Column(nullable = false)
    private LocalDateTime created;

    /**
     * Текст комментария.
     * Должен содержать от 3 до 5000 символов.
     * Не может быть пустым или состоять только из пробелов.
     */
    @Column(nullable = false, length = 5000)
    private String text;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}