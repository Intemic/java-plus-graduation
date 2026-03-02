package ru.yandex.practicum.telemetry.analyzer.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;
import ru.yandex.practicum.telemetry.analyzer.handler.hub.HubEventHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HubEventProcessor extends BaseProcessor<String, HubEventAvro> implements Runnable {
    private final ObjectMapper objectMapper;
    private final Map<Class<? extends SpecificRecordBase>, HubEventHandler<? extends SpecificRecordBase>> handlers;

    public HubEventProcessor(@Autowired KafkaConfig config,
                             @Autowired Set<HubEventHandler<? extends SpecificRecordBase>> hubEventHandlers,
                             @Autowired ObjectMapper objectMapper) {
        super(config.getServerConfig(), config.getConsumers().getHubEvent());
        this.handlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getType,
                        Function.identity()
                ));
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        start();
    }

    @Override
    public void process(ConsumerRecord<String, HubEventAvro> record) {
        Class<?> classPayload = record.value().getPayload().getClass();

        if (!handlers.containsKey(classPayload))
            log.info("Не найден обработчик для класса -%s".formatted(classPayload));

        if (handlers.containsKey(classPayload)) {
            try {
                log.info("Пришло событие - %s".formatted(objectMapper.writeValueAsString(record.value())));
            } catch (JsonProcessingException e) {
                log.info("Пришло событие - %s".formatted(record.value().toString()));
            }

            handlers.get(classPayload).handle(record.value());
            log.info("Данные события сохранены");
        }

    }

    @Override
    public void fixOffset(ConsumerRecord<String, HubEventAvro> record) {
        // здесь не нужно
    }

    @Override
    public void fixCommit() {
        consumer.commitSync();
    }
}
