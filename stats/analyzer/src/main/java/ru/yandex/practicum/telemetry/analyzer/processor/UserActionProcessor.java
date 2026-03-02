package ru.yandex.practicum.telemetry.analyzer.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;
import ru.yandex.practicum.telemetry.analyzer.model.Interaction;
import ru.yandex.practicum.telemetry.analyzer.repository.InteractionRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class UserActionProcessor extends BaseProcessor<String, UserActionAvro> implements Runnable {
    private final ObjectMapper objectMapper;
    private final Map<Long, Map<Long, Double>> mapEventUser = new HashMap<>();
    private final InteractionRepository repository;
    private Map<Long, Map<Long, Double>> mapPackage = new HashMap<>();

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
    public void process(ConsumerRecord<String, UserActionAvro> record) {
        try {
            log.info("Пришло событие - %s".formatted(objectMapper.writeValueAsString(record.value())));
        } catch (JsonProcessingException e) {
            log.info("Пришло событие - %s".formatted(record.value().toString()));
        }

        UserActionAvro userAction = record.value();

        // пытаемся найти уже закэшированное событие
        if (!mapEventUser.containsKey(userAction.getEventId())
                || !mapEventUser.containsKey(userAction.getUserId())) {
            // не нашли считываем из БД
            Interaction interaction = Interaction.builder()
                            .eventId(userAction.getEventId())
                            .userId(userAction.getUserId())
//                            .raiting(0.0)
                            .timeStamp(LocalDate.now())
                            .build();


//           Interaction interaction = repository.findByUserIdAndEventId(userAction.getEventId(), userAction.getUserId())
//                    .orElse(Interaction.builder()
//                            .id(0)
//                            .eventId(userAction.getEventId())
//                            .userId(userAction.getUserId())
//                            .raiting(0.0)
//                            .timeStamp(LocalDate.now())
//                            .build());
        }

        log.info("Данные события сохранены");
    }

    @Override
    public void fixOffset(ConsumerRecord<String, UserActionAvro> record) {

    }

    @Override
    public void fixCommit() {
        consumer.commitSync();
    }
}
