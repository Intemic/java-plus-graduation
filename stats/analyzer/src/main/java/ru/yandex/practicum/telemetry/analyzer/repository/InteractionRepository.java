package ru.yandex.practicum.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.model.Interaction;

import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Long, Interaction> {
    Optional<Interaction> findByUserIdAndEventId(long userId, long eventId);
}
