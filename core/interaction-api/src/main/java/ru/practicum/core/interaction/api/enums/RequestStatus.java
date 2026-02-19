package ru.practicum.core.interaction.api.enums;

/**
 * Перечисление статусов заявок на участие в событиях.
 */
public enum RequestStatus {
    PENDING,    // В ожидании
    CONFIRMED,  // Подтверждено
    CANCELED,   // Отменено
    REJECTED    // Отклонено
}