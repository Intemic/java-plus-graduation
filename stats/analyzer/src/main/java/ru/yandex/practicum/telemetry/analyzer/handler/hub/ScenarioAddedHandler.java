package ru.yandex.practicum.telemetry.analyzer.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScenarioAddedHandler implements HubEventHandler<ScenarioAddedEventAvro> {
    private final ScenarioRepository repository;

    @Override
    public Class<ScenarioAddedEventAvro> getType() {
        return ScenarioAddedEventAvro.class;
    }

    @Override
    @Transactional
    public void handle(HubEventAvro event) {
        ScenarioAddedEventAvro scenarioAdded = (ScenarioAddedEventAvro) event.getPayload();
        Map<String, Condition> conditionMap = scenarioAdded.getConditions().stream()
                .collect(Collectors.toMap(
                        ScenarioConditionAvro::getSensorId,
                        conditionAvro -> Condition.builder()
                                .type(conditionAvro.getType())
                                .operation(conditionAvro.getOperation())
                                .value(convertValue(conditionAvro.getValue()))
                                .build()));
        Map<String, Action> actionMap = scenarioAdded.getActions().stream()
                .collect(Collectors.toMap(
                        DeviceActionAvro::getSensorId,
                        deviceActionAvro -> Action.builder()
                                .type(deviceActionAvro.getType())
                                .value(deviceActionAvro.getValue())
                                .build()
                ));

        Scenario scenario = Scenario.builder()
                .hubId(event.getHubId())
                .name(scenarioAdded.getName())
                .conditions(conditionMap)
                .actions(actionMap)
                .build();

        repository.save(scenario);
    }

    private Integer convertValue(Object value) {
        if (value == null)
            return null;

        if (value instanceof Boolean)
            return (Boolean) value == true ? 1 : 0;

        return (Integer) value;
    }
}
