package ru.practicum.request.service.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.utill.State;
import ru.practicum.core.interaction.api.exception.ConflictResource;
import ru.practicum.core.interaction.api.exception.NotFoundResource;
import ru.practicum.core.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.request.service.request.mapper.RequestMapper;
import ru.practicum.request.service.request.model.Request;
import ru.practicum.request.service.request.repository.RequestRepository;
import ru.practicum.core.interaction.api.enums.RequestStatus;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с заявками на участие в событиях.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

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
        User user = getUserById(userId);
        Event event = getEventById(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictResource("Инициатор события не может подать заявку на участие в своём событии");
        }

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictResource("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictResource("Заявка на участие в этом событии уже существует");
        }

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictResource("Достигнут лимит участников для этого события");
        }

        RequestStatus status = RequestStatus.PENDING;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                //.event(event)
                .eventId(event.getId())
                //.requester(user)
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

        request.setStatus(RequestStatus.CANCELED);
        Request updatedRequest = requestRepository.save(request);

        return RequestMapper.mapToParticipationRequestDto(updatedRequest);
    }

    @Override
    public List<ParticipationRequestDto> findByEventId(Long eventId) {
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipationRequestDto> findByIdInAndEventId(Long eventId, List<Long> requestIds) {
        return requestRepository.findByIdInAndEventId(requestIds, eventId).stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long countByEventIdAndStatus(Long eventId, RequestStatus status) {
        return requestRepository.countByEventIdAndStatus(eventId, status);
    }

    @Override
    public void updateStatus(List<ParticipationRequestDto> requestsDto) {
       requestRepository.saveAll(requestsDto.stream().map(RequestMapper::mapFromDto).toList());
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundResource("Пользователь с id=" + userId + " не найден"));
    }

    private Event getEventById(Long eventId) {
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