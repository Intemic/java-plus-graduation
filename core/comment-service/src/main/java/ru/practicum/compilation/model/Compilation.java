package ru.practicum.compilation.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

/**
 * Сущность подборки событий.
 */
@Builder(toBuilder = true)
@Table(name = "compilations")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false)
    @Builder.Default
    private Boolean pinned = false;

    @ElementCollection
    @CollectionTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id")
    )
    @Column(name = "event_id")
    private Set<Long> events;
}