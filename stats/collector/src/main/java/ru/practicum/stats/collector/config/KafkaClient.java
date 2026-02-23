package ru.practicum.stats.collector.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

public interface KafkaClient {
    Producer<Long, SpecificRecordBase> getProducer();

    void stop();
}
