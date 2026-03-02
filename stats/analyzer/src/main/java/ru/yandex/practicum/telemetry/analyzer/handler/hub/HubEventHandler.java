package ru.yandex.practicum.telemetry.analyzer.handler.hub;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public interface HubEventHandler<T extends SpecificRecordBase> {
    Class<T> getType();

    void handle(HubEventAvro event);
}
