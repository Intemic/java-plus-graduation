package ru.yandex.practicum.telemetry.analyzer.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;
import ru.yandex.practicum.telemetry.analyzer.model.Similaritie;
import ru.yandex.practicum.telemetry.analyzer.repository.SimilaritieRepository;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Component
public class EventsSimilarityProcessor extends BaseProcessor<String, EventSimilarityAvro> implements Runnable {
    private final ObjectMapper objectMapper;
    private final SimilaritieRepository repository;

    public EventsSimilarityProcessor(@Autowired KafkaConfig config,
                                     @Autowired ObjectMapper objectMapper,
                                     @Autowired SimilaritieRepository repository) {
        super(config.getServerConfig(), config.getConsumers().getEventsSimilarity());
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @Override
    public void run() {
        //
    }

    @Override
    @Transactional
    public void process(ConsumerRecord<String, EventSimilarityAvro> record) {
        try {
            log.info("Пришло событие - %s".formatted(objectMapper.writeValueAsString(record.value())));
        } catch (JsonProcessingException e) {
            log.info("Пришло событие - %s".formatted(record.value().toString()));
        }

        EventSimilarityAvro eventAvro = record.value();
        Optional<Similaritie> optionalSimilaritie = repository
                .findByEventAAndEventB(eventAvro.getEventA(), eventAvro.getEventB());
        Similaritie similaritie = null;

        if (optionalSimilaritie.isEmpty()) {
            similaritie = Similaritie.builder()
                    .eventA(eventAvro.getEventA())
                    .eventB(eventAvro.getEventB())
                    .score(eventAvro.getScore())
                    .timeStamp(LocalDate.now())
                    .build();
        } else {
            similaritie = optionalSimilaritie.get();
            similaritie.setScore(eventAvro.getScore());
        }

        if (similaritie != null) {
            repository.save(similaritie);
            log.info("Данные события сохранены");
        } else {
            log.info("Обновление не требуется");
        }
    }
}
