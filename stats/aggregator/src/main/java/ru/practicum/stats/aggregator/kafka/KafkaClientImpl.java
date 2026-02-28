package ru.practicum.stats.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.aggregator.config.AggregatorConfig;

import java.time.Duration;
import java.util.Properties;

@Slf4j
//@Component
@RequiredArgsConstructor
public class KafkaClientImpl implements KafkaClient {
    private final AggregatorConfig config;
    private Producer<String, EventSimilarityAvro> producer;
    private Consumer<String, UserActionAvro> consumer;

    @Override
    public Producer<String, EventSimilarityAvro> getProducer() {
        if (producer == null)
            initProducer();

        return producer;
    }

    @Override
    public Consumer<String, UserActionAvro> getConsumer() {
        if (consumer == null)
            initConsumer();

        return consumer;
    }

    public void stop() {
        if (producer != null) {
            log.info("Закрываем консьюмер");
            producer.close(Duration.ofSeconds(10));
        }

        if (consumer != null) {
            log.info("Закрываем продюсер");
            consumer.close();
        }
    }

    private void initProducer() {
        log.info("Создаем продюсер");
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafka().getMain().getServerConfig());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                config.getKafka().getMain().getProducer().getKeySerializer());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                config.getKafka().getMain().getProducer().getValueSerializer());
        producer = new KafkaProducer<>(properties);
    }

    private void initConsumer() {
        log.info("Создаем консьюмер");
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafka().getMain().getServerConfig());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                config.getKafka().getMain().getConsumer().getKeyDeserializer());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                config.getKafka().getMain().getConsumer().getValueDeserializer());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, config.getKafka().getMain().getConsumer().getGroupId());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                config.getKafka().getMain().getConsumer().getAutoOffsetReset());
        consumer = new KafkaConsumer<>(properties);
    }

}
