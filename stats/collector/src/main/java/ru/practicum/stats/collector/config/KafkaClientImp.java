package ru.practicum.stats.collector.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class KafkaClientImp implements KafkaClient {
    private Producer<Long, SpecificRecordBase> producer;
    private final CollectorConfig config;

    public Producer<Long, SpecificRecordBase> getProducer() {
        if (producer == null)
            initProducer();

        return producer;
    }


    public void stop() {
        if (producer != null) {
            // отправляем оставшиеся данные и закрываем продюсер
            producer.flush();
            producer.close(Duration.ofSeconds(10));
        }

    }

    private void initProducer() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafka().getMain().getServerConfig());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getKafka().getSerializerClass().getKey());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getKafka().getSerializerClass().getValue());
        producer = new KafkaProducer<>(properties);
    }
}
