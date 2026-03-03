package ru.yandex.practicum.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.model.Similaritie;

import java.util.Optional;

public interface SimilaritieRepository extends JpaRepository<Similaritie, Long> {
    Optional<Similaritie> findByEventAAndEventB(long eventA, long eventB);
}
