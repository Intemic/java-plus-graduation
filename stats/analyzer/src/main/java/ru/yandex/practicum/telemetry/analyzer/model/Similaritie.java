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
    private long id;

    @Column(name = "event_a")
    private long eventA;

    @Column(name = "event_b")
    private long eventB;

    private double scope;

    @Column(name = "time_stamp")
    private LocalDate timeStamp;
}
