package ru.yandex.practicum.telemetry.analyzer.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;
import ru.yandex.practicum.telemetry.analyzer.model.Interaction;
import ru.yandex.practicum.telemetry.analyzer.repository.InteractionRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class UserActionProcessor extends BaseProcessor<String, UserActionAvro> implements Runnable {
    private final ObjectMapper objectMapper;
    private final InteractionRepository repository;

    public UserActionProcessor(@Autowired KafkaConfig config,
                               @Autowired ObjectMapper objectMapper,
                               @Autowired InteractionRepository repository) {
        super(config.getServerConfig(), config.getConsumers().getUserActions());
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @Override
    public void run() {
        start();
    }

    @Override
    @Transactional
    public void process(ConsumerRecord<String, UserActionAvro> record) {
        try {
            log.info("Пришло событие - %s".formatted(objectMapper.writeValueAsString(record.value())));
        } catch (JsonProcessingException e) {
            log.info("Пришло событие - %s".formatted(record.value().toString()));
        }

        UserActionAvro userAction = record.value();
        Optional<Interaction> optionalInteraction = repository
                .findByUserIdAndEventId(userAction.getUserId(), userAction.getEventId());
        Interaction interaction = null;

        if (optionalInteraction.isEmpty()) {
            interaction = Interaction.builder()
                    .userId(userAction.getUserId())
                    .eventId(userAction.getEventId())
                    .rating(getWeightForAction(userAction.getActionType()))
                    .timeStamp(LocalDate.now())
                    .build();
        } else if (optionalInteraction.get().getRating() < getWeightForAction(userAction.getActionType())) {
            interaction = optionalInteraction.get();
            interaction.setRating(getWeightForAction(userAction.getActionType()));
        }

        if (interaction != null) {
            repository. save(interaction);
            log.info("Данные события сохранены");
        } else {
            log.info("Обновление не требуется");
        }
    }

    private double getWeightForAction(ActionTypeAvro action) {
        return switch (action) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
