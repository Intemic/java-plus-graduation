package ru.practicum.request.service.request.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.core.interaction.api.enums.RequestStatus;
import ru.practicum.core.interaction.api.interface_.RequestOperation;
import ru.practicum.request.service.request.service.RequestService;

import java.util.Collection;
import java.util.List;

/**
 * Контроллер для операций с заявками на участие в событиях.
 */
@Validated
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestController implements RequestOperation {

    private final RequestService requestService;

    /**
     * Получает все заявки пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список заявок
     */
    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable @Positive Long userId) {
        return requestService.getRequestsByUserId(userId);
    }

//    @GetMapping("/events/{eventId}/requests")
//    public List<ParticipationRequestDto> findByEventId(@PathVariable @Positive Long eventId,
//                                                       @RequestParam(required = false) List<Long> requestIds) {
//        if (requestIds == null)
//            return requestService.findByEventId(eventId);
//
//        return requestService.findByIdInAndEventId(eventId, requestIds);
//
//    }

//    @GetMapping("/events/{eventId}/status/{status}/count")
//    Long countByEventIdAndStatus(@PathVariable @Positive Long eventId, @NotNull RequestStatus status) {
//        return requestService.countByEventIdAndStatus(eventId, status);
//    }


    /**
     * Создает новую заявку на участие в событии.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return созданная заявка
     */
    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Positive Long userId,
                                                 @RequestParam @Positive Long eventId) {
        return requestService.createRequest(userId, eventId);
    }

    /**
     * Отменяет заявку на участие.
     *
     * @param userId    идентификатор пользователя
     * @param requestId идентификатор заявки
     * @return отмененная заявка
     */
    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    @Override
    public List<ParticipationRequestDto> findByEventId(Long eventId, List<Long> requestIds) {
        return List.of();
    }

    @Override
    public Long countByEventIdAndStatus(Long eventId, RequestStatus status) {
        return 0L;
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventIdInAndStatus(RequestStatus status, Collection<Long> eventIds) {
        return List.of();
    }

    @Override
    public void updateStatus(List<ParticipationRequestDto> requests) {

    }

//    @PatchMapping("/requests/update-status")
//    void updateStatus(@RequestBody @NotNull List<ParticipationRequestDto> requestsDto) {
//        requestService.updateStatus(requestsDto);
//    }
//
//    @GetMapping("/events/status/{status}")
//    List<ParticipationRequestDto> findAllByEventIdInAndStatus(@NotNull RequestStatus status,
//                                                              @RequestParam Collection<Long> eventIds) {
//
//    }


}