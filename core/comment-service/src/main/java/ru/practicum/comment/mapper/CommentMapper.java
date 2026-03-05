package ru.practicum.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.core.interaction.api.dto.event.EventFullDto;
import ru.practicum.core.interaction.api.dto.event.EventShortDto;
import ru.practicum.core.interaction.api.dto.user.UserDto;

import java.util.Map;

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
                .eventId(commentDto.getEvent())
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
    public static CommentDto mapFromComment(Comment comment,
                                            Map<Long, UserDto> userDtoMap,
                                            Map<Long, EventFullDto> eventDtoMap) {
        if (comment == null) {
            throw new IllegalArgumentException("Comment не может быть null");
        }

        return CommentDto.builder()
                .id(comment.getId())
                .author(toUserDto(comment.getAuthorId(), userDtoMap))
                .event(mapToEventShortDto(comment.getEventId(), eventDtoMap))
                .created(comment.getCreated())
                .text(comment.getText())
                .build();
    }

    public static UserDto toUserDto(long userId, Map<Long, UserDto> userDtoMap) {
        if (userDtoMap == null || userId == 0) {
            return null;
        }

        UserDto userDto = null;
        if (userDtoMap.containsKey(userId))
            userDto = userDtoMap.get(userId);

        return userDto;
    }

    public static EventShortDto mapToEventShortDto(Long eventId, Map<Long, EventFullDto> eventDtoMap) {
        if (eventDtoMap == null || eventDtoMap.isEmpty())
            return null;

        EventFullDto event = eventDtoMap.get(eventId);

        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(event.getCategory())
                .initiator(event.getInitiator())
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .confirmedRequests(event.getConfirmedRequests())
                .rating(event.getRating())
                .build();
    }
}