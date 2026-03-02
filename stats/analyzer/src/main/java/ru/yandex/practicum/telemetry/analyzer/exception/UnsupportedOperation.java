package ru.yandex.practicum.telemetry.analyzer.exception;

public class UnsupportedOperation extends RuntimeException {
    public UnsupportedOperation(String msg) {
        super(msg);
    }

    public UnsupportedOperation(String msg, Throwable cause) {
        super(msg, cause);
    }
}