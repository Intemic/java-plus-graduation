package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "similarities")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Similaritie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_a")
    private Long eventA;

    @Column(name = "event_b")
    private Long eventB;

    private Double score;

    @Column(name = "time_stamp")
    private LocalDate timeStamp;
}
