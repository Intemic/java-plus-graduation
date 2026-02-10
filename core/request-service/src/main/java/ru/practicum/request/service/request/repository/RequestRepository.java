package ru.practicum.request.service.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.request.service.request.model.Request;
import ru.practicum.core.interaction.api.enums.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с заявками на участие в событиях.
 * <p>
 * Предоставляет методы для выполнения операций с заявками, включая поиск по событию,
 * <p>
 * Инициатору, статусу и сложные запросы с использованием JPQL.
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    /**
     * Находит все заявки для указанного события.
     *
     * @param eventId идентификатор события
     * @return список заявок
     */
    List<Request> findByEventId(Long eventId);

    /**
     * Находит заявки по списку идентификаторов и идентификатору события.
     *
     * @param requestIds список идентификаторов заявок
     * @param eventId    идентификатор события
     * @return список заявок
     */
    List<Request> findByIdInAndEventId(List<Long> requestIds, Long eventId);

    /**
     * Подсчитывает количество заявок для события с указанным статусом.
     *
     * @param eventId идентификатор события
     * @param status  статус заявки
     * @return количество заявок
     */
    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    /**
     * Находит заявку по идентификатору события и идентификатору requester'а.
     *
     * @param eventId     идентификатор события
     * @param requesterId идентификатор пользователя
     * @return Optional с заявкой, если найдена
     */
    Optional<Request> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    /**
     * Находит все заявки пользователя.
     *
     * @param requesterId идентификатор пользователя
     * @return список заявок
     */
    List<Request> findByRequesterId(Long requesterId);

    /**
     * Находит все заявки для списка событий с указанным статусом.
     *
     * @param eventIds коллекция идентификаторов событий
     * @param status   статус заявки
     * @return список заявок
     */
    //@Query("SELECT r FROM Request r WHERE r.event.id IN :eventIds AND r.status = :status")
    @Query("SELECT r FROM Request r WHERE r.eventId IN :eventIds AND r.status = :status")
    List<Request> findAllByEventIdInAndStatus(@Param("eventIds") Collection<Long> eventIds,
                                              @Param("status") RequestStatus status);

    /**
     * Находит заявки для события с указанным статусом.
     *
     * @param eventId идентификатор события
     * @param status  статус заявки
     * @return список заявок
     */
    List<Request> findByEventIdAndStatus(Long eventId, RequestStatus status);

    /**
     * Проверяет существование заявки по идентификатору пользователя и события.
     *
     * @param requesterId идентификатор пользователя
     * @param eventId     идентификатор события
     * @return true если заявка существует, иначе false
     */
    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    /**
     * Подсчитывает количество подтвержденных заявок для события.
     *
     * @param eventId идентификатор события
     * @return количество подтвержденных заявок
     */
    //@Query("SELECT COUNT(r) FROM Request r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    @Query("SELECT COUNT(r) FROM Request r WHERE r.eventId = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    /**
     * Находит заявки по идентификатору инициатора и идентификатору события.
     *
     * @param initiatorId идентификатор инициатора события
     * @param eventId     идентификатор события
     * @return список заявок
     */
    //List<Request> findByEventInitiatorIdAndEventId(Long initiatorId, Long eventId);

    /**
     * Находит заявку по идентификатору события и пользователя с использованием JPQL.
     *
     * @param eventId     идентификатор события
     * @param requesterId идентификатор пользователя
     * @return Optional с заявкой, если найдена
     */
    //@Query("SELECT r FROM Request r WHERE r.event.id = :eventId AND r.requester.id = :requesterId")
    @Query("SELECT r FROM Request r WHERE r.eventId = :eventId AND r.requesterId = :requesterId")
    Optional<Request> findByEventIdAndRequesterIdWithEvent(@Param("eventId") Long eventId,
                                                           @Param("requesterId") Long requesterId);

    /**
     * Подсчитывает количество заявок для события с указанным статусом с использованием JPQL.
     *
     * @param eventId идентификатор события
     * @param status  статус заявки
     * @return количество заявок
     */
    //@Query("SELECT COUNT(r) FROM Request r WHERE r.event.id = :eventId AND r.status = :status")
    @Query("SELECT COUNT(r) FROM Request r WHERE r.eventId = :eventId AND r.status = :status")
    Long countByEventIdAndStatusQuery(@Param("eventId") Long eventId, @Param("status") RequestStatus status);

    /**
     * Находит заявки по статусу и списку идентификаторов событий.
     *
     * @param status   статус заявки
     * @param eventIds список идентификаторов событий
     * @return список заявок
     */
    List<Request> findByStatusAndEventIdIn(RequestStatus status, List<Long> eventIds);

    /**
     * Находит заявки по идентификатору события и списку идентификаторов заявок.
     *
     * @param eventId    идентификатор события
     * @param requestIds список идентификаторов заявок
     * @return список заявок
     */
    //@Query("SELECT r FROM Request r WHERE r.event.id = :eventId AND r.id IN :requestIds")
    @Query("SELECT r FROM Request r WHERE r.eventId = :eventId AND r.id IN :requestIds")
    List<Request> findByEventIdAndIdIn(@Param("eventId") Long eventId, @Param("requestIds") List<Long> requestIds);

    /**
     * Находит заявку по идентификатору и идентификатору пользователя.
     *
     * @param requestId   идентификатор заявки
     * @param requesterId идентификатор пользователя
     * @return Optional с заявкой, если найдена
     */
    Optional<Request> findByIdAndRequesterId(Long requestId, Long requesterId);
}