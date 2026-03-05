package ru.yandex.practicum.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.telemetry.analyzer.model.Similaritie;

import java.util.List;
import java.util.Optional;

public interface SimilaritieRepository extends JpaRepository<Similaritie, Long> {
    Optional<Similaritie> findByEventAAndEventB(long eventA, long eventB);

    List<Similaritie> findAllByEventAOrEventB(long eventIdA, long eventIdB);

    List<Similaritie> findAllByEventAInOrEventBIn(List<Long> eventIdsA, List<Long> eventIdsB);

}
