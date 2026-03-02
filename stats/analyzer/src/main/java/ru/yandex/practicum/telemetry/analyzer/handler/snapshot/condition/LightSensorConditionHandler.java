package ru.yandex.practicum.telemetry.analyzer.handler.snapshot.condition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorEventAvro;
import ru.yandex.practicum.telemetry.analyzer.exception.IllegalTypeCondition;
import ru.yandex.practicum.telemetry.analyzer.exception.UnsupportedOperation;
import ru.yandex.practicum.telemetry.analyzer.handler.snapshot.operation.CompareOperationManager;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;

@Component
@RequiredArgsConstructor
public class LightSensorConditionHandler implements ConditionHandler<Condition, LightSensorEventAvro> {
    private final CompareOperationManager compareManager;

    @Override
    public Class<LightSensorEventAvro> getType() {
        return LightSensorEventAvro.class;
    }

    @Override
    public boolean check(Condition condition, LightSensorEventAvro event) {
        if (event == null)
            return false;

        try {
            Integer sensorValue = switch (condition.getType()) {
                case LUMINOSITY -> event.getLuminosity();
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
