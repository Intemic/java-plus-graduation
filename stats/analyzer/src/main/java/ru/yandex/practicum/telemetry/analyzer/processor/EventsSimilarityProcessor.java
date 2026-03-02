package ru.yandex.practicum.telemetry.analyzer.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;

public class EventsSimilarityProcessor  extends BaseProcessor<String, EventSimilarityAvro> implements Runnable  {
    private final ObjectMapper objectMapper;

    public EventsSimilarityProcessor(@Autowired KafkaConfig config,
                                     @Autowired ObjectMapper objectMapper) {
        super(config.getServerConfig(), config.getConsumers().getEventsSimilarity());
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {

    }

    @Override
    public void process(ConsumerRecord<String, EventSimilarityAvro> record) {

    }

    @Override
    public void fixOffset(ConsumerRecord<String, EventSimilarityAvro> record) {

    }

    @Override
    public void fixCommit() {
        consumer.commitSync();
    }
}
