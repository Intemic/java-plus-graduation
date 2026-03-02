package ru.yandex.practicum.telemetry.analyzer.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;

public class UserActionProcessor extends BaseProcessor<String, UserActionAvro> implements Runnable {
    private final ObjectMapper objectMapper;

    public UserActionProcessor(@Autowired KafkaConfig config,
                               @Autowired ObjectMapper objectMapper) {
        super(config.getServerConfig(), config.getConsumers().getUserActions());
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {

    }

    @Override
    public void process(ConsumerRecord<String, UserActionAvro> record) {

    }

    @Override
    public void fixOffset(ConsumerRecord<String, UserActionAvro> record) {

    }

    @Override
    public void fixCommit() {
        consumer.commitSync();
    }
}
