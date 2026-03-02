package ru.yandex.practicum.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.model.Similaritie;

public interface SimilaritieRepository extends JpaRepository<Long, Similaritie> {
}
