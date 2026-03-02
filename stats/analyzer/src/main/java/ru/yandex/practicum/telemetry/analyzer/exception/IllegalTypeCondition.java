package ru.yandex.practicum.telemetry.analyzer.exception;

public class IllegalTypeCondition extends RuntimeException {
    public IllegalTypeCondition(String msg) {
        super(msg);
    }

    public IllegalTypeCondition(String msg, Throwable cause) {
        super(msg, cause);
    }
}
