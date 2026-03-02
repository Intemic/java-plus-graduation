package ru.yandex.practicum.telemetry.analyzer.handler.snapshot.operation;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.OperationAvro;

@Component
public class CompareOperationLowerThan implements CompareOperation<Integer, Integer, OperationAvro>{
    @Override
    public OperationAvro getType() {
        return OperationAvro.LOWER_THAN;
    }

    @Override
    public boolean compare(Integer reference, Integer value) {
        if (reference == null || value == null )
            return false;

        return reference > value;
    }
}
