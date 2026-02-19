package ru.practicum.core.interaction.api.exception;

/**
 * Исключение при нарушении доступа к ресурсу.
 */
public class ForbiddenResource extends RuntimeException {
    public ForbiddenResource(String msg) {
        super(msg);
    }
}
