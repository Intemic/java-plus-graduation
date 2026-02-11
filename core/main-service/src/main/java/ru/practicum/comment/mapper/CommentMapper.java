package ru.practicum.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.core.interaction.api.client.UserClient;
import ru.practicum.core.interaction.api.dto.user.UserDto;
import ru.practicum.event.mapper.EventMapper;

import java.util.Optional;

/**
 * Утилитарный класс для преобразования между сущностями Comment и DTO.
 */
@UtilityClass
public class CommentMapper {

    /**
     * Преобразует DTO для создания комментария в сущность Comment.
     *
     * @param commentDto DTO с данными для создания комментария
     * @return сущность Comment, подготовленная для сохранения в базу данных
     * @throws IllegalArgumentException если commentDto равен null
     */
    public static Comment mapFromNewDto(NewCommentDto commentDto) {
        if (commentDto == null) {
            throw new IllegalArgumentException("NewCommentDto не может быть null");
        }

        return Comment.builder()
                .authorId(commentDto.getAuthor())
                .event(commentDto.getEventObj())
                .created(commentDto.getCreated())
                .text(commentDto.getText())
                .build();
    }

    /**
     * Преобразует сущность Comment в DTO для ответа.
     * Включает преобразование связанных сущностей User и Event в соответствующие DTO.
     *
     * @param comment сущность комментария из базы данных
     * @return DTO комментария для возврата в API
     * @throws IllegalArgumentException если comment равен null
     */
    public static CommentDto mapFromComment(Comment comment, UserClient userRepository) {
        if (comment == null) {
            throw new IllegalArgumentException("Comment не может быть null");
        }

        return CommentDto.builder()
                .id(comment.getId())
                .author(toUserDto(comment.getAuthorId(), userRepository))
                .event(EventMapper.mapToEventShortDto(comment.getEvent(), userRepository))
                .created(comment.getCreated())
                .text(comment.getText())
                .build();
    }

    public static UserDto toUserDto(long userId, UserClient userClient) {
        if (userClient == null || userId == 0) {
            return null;
        }

        Optional<UserDto> userDtoOptional = userClient.findById(userId);
        return userDtoOptional.orElse(null);

    }
}