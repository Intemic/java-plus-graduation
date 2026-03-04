package ru.yandex.practicum.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.model.Interaction;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    Optional<Interaction> findByUserIdAndEventId(long userId, long eventId);

    List<Interaction> findAllByEventIdIn(List<Long> eventId);

    List<Interaction> findAllByEventIdInAndUserIdNot(Collection<Long> eventIds, long exclUserId);

    List<Interaction> findAllByUserIdOrderByTimeStampDesc(long userId, Pageable page);
}
