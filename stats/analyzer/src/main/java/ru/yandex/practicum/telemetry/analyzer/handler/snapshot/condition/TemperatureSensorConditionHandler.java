package ru.yandex.practicum.telemetry.analyzer.handler.snapshot.condition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorEventAvro;
import ru.yandex.practicum.telemetry.analyzer.exception.IllegalTypeCondition;
import ru.yandex.practicum.telemetry.analyzer.exception.UnsupportedOperation;
import ru.yandex.practicum.telemetry.analyzer.handler.snapshot.operation.CompareOperationManager;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;

@Component
@RequiredArgsConstructor
public class TemperatureSensorConditionHandler implements ConditionHandler<Condition, TemperatureSensorEventAvro> {
    private final CompareOperationManager compareManager;

    @Override
    public Class<TemperatureSensorEventAvro> getType() {
        return TemperatureSensorEventAvro.class;
    }

    @Override
    public boolean check(Condition condition, TemperatureSensorEventAvro event) {
        if (event == null)
            return false;

        try {
            Integer sensorValue = switch (condition.getType()) {
                case TEMPERATURE -> event.getTemperatureC();
                default -> throw new IllegalTypeCondition("Не поддерживаемый тип условия: " + condition.getType());
            };

            return compareManager.getCompareOperation(condition.getOperation())
                    .compare(condition.getValue(), sensorValue);

        } catch (IllegalTypeCondition e) {
            throw e;
        } catch (NullPointerException e) {
            throw new UnsupportedOperation("Не поддерживаемый тип операции % s".formatted(condition.getOperation()));
        }
    }
}
