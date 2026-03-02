package ru.yandex.practicum.telemetry.analyzer.handler.snapshot.operation;

public interface CompareOperation<R, V, T> {
    T getType( );

    boolean compare(R reference , V value);
}
