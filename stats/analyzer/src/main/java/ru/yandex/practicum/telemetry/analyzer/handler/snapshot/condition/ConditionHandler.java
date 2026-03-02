package ru.yandex.practicum.telemetry.analyzer.handler.snapshot.condition;

public interface ConditionHandler<C, O>  {
    Class<O> getType();

    boolean check(C condition, O object);
}
