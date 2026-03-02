package ru.yandex.practicum.telemetry.analyzer.exception;

public class UnsupportedSensor extends RuntimeException {
    public UnsupportedSensor(String msg) {
        super(msg);
    }

    public UnsupportedSensor(String msg, Throwable cause) {
        super(msg, cause);
    }
}
