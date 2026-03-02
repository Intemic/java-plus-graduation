package ru.yandex.practicum.telemetry.analyzer.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.ActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;
import ru.yandex.practicum.telemetry.analyzer.exception.UnsupportedSensor;
import ru.yandex.practicum.telemetry.analyzer.handler.snapshot.condition.ConditionHandler;
import ru.yandex.practicum.telemetry.analyzer.handler.snapshot.condition.ConditionHandlerManager;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class SnapshotProcessor extends BaseProcessor<String, SensorsSnapshotAvro> {
    private final int MAX_COUNT_PROCESSED_RECORDS = 10;
    private final ScenarioRepository repository;
    private final ObjectMapper objectMapper;
    private int processedRecord;
    private final ConditionHandlerManager conditionManager;
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public SnapshotProcessor(@Autowired KafkaConfig config,
                             @Autowired ScenarioRepository repository,
                             @Autowired ConditionHandlerManager manager,
                             @Autowired ObjectMapper objectMapper,
                             @GrpcClient("hub-router")
                             HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient) {
        super(config.getServerConfig(), config.getConsumers().getSnapshot());
        this.repository = repository;
        this.conditionManager = manager;
        this.objectMapper = objectMapper;
        this.hubRouterClient = hubRouterClient;
    }

    @Override
    public void process(ConsumerRecord<String, SensorsSnapshotAvro> record) {
        String json;
        try {
            json = objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            json = record.toString();
        }

        log.info("Пришло событие SensorsSnapshotAvro - %s".formatted(record));

        repository.findByHubId(record.value().getHubId()).stream()
                .filter(scenario ->
                        checkScenario(scenario, record.value()))
                .map(scenario -> mapToActionRequests(record.value().getHubId(), scenario))
                .filter(Objects::nonNull)
                .forEach(actionRequests ->
                        actionRequests.forEach(this::sendRequest));
    }

    @Override
    public void fixOffset(ConsumerRecord<String, SensorsSnapshotAvro> record) {
        currentOffsets.put(new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1));

        if (processedRecord % MAX_COUNT_PROCESSED_RECORDS == 0)
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });

        processedRecord++;
    }

    @Override
    public void fixCommit() {
        // здесь не нужно
    }

    private boolean checkScenario(Scenario scenario, SensorsSnapshotAvro snapshot) {
        String json;
        ConditionHandler<Condition, SpecificRecordBase> handler;

        try {
            json = objectMapper.writeValueAsString(scenario.getConditions());
        } catch (JsonProcessingException e) {
            json = scenario.getConditions().toString();
        }
        log.info("Проверяем сценарий - %s = %s".formatted(scenario.getName(), json));

        for (Map.Entry<String, Condition> entry : scenario.getConditions().entrySet()) {
            try {
                SpecificRecordBase sensorData = (SpecificRecordBase) snapshot.getSensorsState().get(entry.getKey()).getData();
                // ищем обработчик
                try {
                    handler = conditionManager.getHandler(sensorData.getClass());
                } catch (NullPointerException e) {
                    throw new UnsupportedSensor("Не поддерживаемый тип сенсора %s".formatted(sensorData.getClass()));
                }

                // проверяем условия
                if (!handler.check(entry.getValue(), sensorData)) {
                    log.info("Сценарий не подошел");
                    return false;
                }

                // отсутствуют данные датчика
            } catch (NullPointerException e) {
                log.info("Сценарий не подошел, ошибка");
                return false;
            } catch (Exception e) {
                log.info("Сценарий не подошел, ошибка");
                log.info(e.getMessage(), e);
                return false;
            }
        }

        log.info("Сценарий подходит");
        return true;
    }

    private List<DeviceActionRequest> mapToActionRequests(String hubId, Scenario scenario) {
        Instant instant = Instant.now();

        List<DeviceActionRequest> actionRequests = new ArrayList<>();

        for (Map.Entry<String, Action> entry : scenario.getActions().entrySet()) {
            DeviceActionProto action = DeviceActionProto.newBuilder()
                    .setSensorId(entry.getKey())
                    .setType(convertAction(entry.getValue().getType()))
                    .setValue(entry.getValue().getValue())
                    .build();

            actionRequests.add(DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenario.getName())
                    .setTimestamp(Timestamp.newBuilder()
                            .setNanos(instant.getNano())
                            .setSeconds(instant.getEpochSecond())
                            .build())
                    .setAction(action)
                    .build());
        }
        return actionRequests;
    }

    private void sendRequest(DeviceActionRequest request) {
        String json;

        try {
            json = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            json = request.toString();
        }

        log.info("Отправляем сообщение: %s".formatted(json));
        hubRouterClient.handleDeviceAction(request);
        log.info("Сообщение отправлено");
    }

    private ActionTypeProto convertAction(ActionAvro actionAvro) {
        return switch (actionAvro) {
            case ACTIVATE -> ActionTypeProto.ACTIVATE;
            case DEACTIVATE -> ActionTypeProto.DEACTIVATE;
            case INVERSE -> ActionTypeProto.INVERSE;
            case SET_VALUE -> ActionTypeProto.SET_VALUE;
        };
    }
}
