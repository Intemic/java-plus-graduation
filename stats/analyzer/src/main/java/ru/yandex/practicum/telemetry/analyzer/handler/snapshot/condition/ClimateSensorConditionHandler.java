package ru.yandex.practicum.telemetry.analyzer.handler.snapshot.condition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorEventAvro;
import ru.yandex.practicum.telemetry.analyzer.exception.IllegalTypeCondition;
import ru.yandex.practicum.telemetry.analyzer.exception.UnsupportedOperation;
import ru.yandex.practicum.telemetry.analyzer.handler.snapshot.operation.CompareOperationManager;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;

@Component
@RequiredArgsConstructor
public class ClimateSensorConditionHandler implements ConditionHandler<Condition, ClimateSensorEventAvro> {
    private final CompareOperationManager compareManager;

    @Override
    public Class<ClimateSensorEventAvro> getType() {
        return ClimateSensorEventAvro.class;
    }

    @Override
    public boolean check(Condition condition, ClimateSensorEventAvro event) {
        if (event == null)
            return false;

        try {
            Integer sensorValue = switch (condition.getType()) {
                case HUMIDITY -> event.getHumidity();
                case TEMPERATURE -> event.getTemperatureC();
                case CO2LEVEL -> event.getCo2Level();
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
