package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.interaction.api.client.EventClient;
import ru.practicum.core.interaction.api.client.UserClient;
import ru.practicum.core.interaction.api.dto.event.EventFullDto;
import ru.practicum.core.interaction.api.dto.user.UserDto;
import ru.practicum.core.interaction.api.enums.EventState;
import ru.practicum.core.interaction.api.exception.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.utill.Status;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Реализация сервиса для работы с заявками на участие в событиях.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserClient userRepository;
    //private final EventClient eventRepository;
    private EventClient eventRepository;

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        checkUserExists(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        UserDto user = getUserById(userId);
        EventFullDto event = getEventById(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictResource("Инициатор события не может подать заявку на участие в своём событии");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictResource("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictResource("Заявка на участие в этом событии уже существует");
        }

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictResource("Достигнут лимит участников для этого события");
        }

        Status status = Status.PENDING;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            status = Status.CONFIRMED;
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .eventId(event.getId())
                .requesterId(user.getId())
                .status(status)
                .build();

        Request savedRequest = requestRepository.save(request);
        return RequestMapper.mapToParticipationRequestDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        checkUserExists(userId);
        Request request = getRequestByIdAndRequesterId(requestId, userId);

        request.setStatus(Status.CANCELED);
        Request updatedRequest = requestRepository.save(request);

        return RequestMapper.mapToParticipationRequestDto(updatedRequest);
    }

    private UserDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundResource("Пользователь с id=" + userId + " не найден"));
    }

    private EventFullDto getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundResource("Событие с id=" + eventId + " не найдено"));
    }

    private Request getRequestByIdAndRequesterId(Long requestId, Long requesterId) {
        return requestRepository.findByIdAndRequesterId(requestId, requesterId)
                .orElseThrow(() -> new NotFoundResource("Заявка с id=" + requestId + " не найдена"));
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundResource("Пользователь с id=" + userId + " не найден");
        }
    }
}