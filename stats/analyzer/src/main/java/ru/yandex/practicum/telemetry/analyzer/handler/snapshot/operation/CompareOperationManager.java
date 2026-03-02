package ru.yandex.practicum.telemetry.analyzer.handler.snapshot.operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.OperationAvro;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CompareOperationManager {
    private final Map<OperationAvro, CompareOperation<Integer, Integer, OperationAvro>> compareOperationMap;

    public CompareOperationManager(
            @Autowired Set<CompareOperation<Integer, Integer, OperationAvro>> compareOperations) {
        this.compareOperationMap = compareOperations.stream()
                .collect(Collectors.toMap(CompareOperation::getType, Function.identity()));
    }

    public CompareOperation getCompareOperation(OperationAvro operation) {
        return compareOperationMap.get(operation);
    }
}
