package ru.practicum.stats.collector.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Properties;

@Component
public class KafaClientImp implements KafkaClient {
    private Producer<String, SpecificRecordBase> producer;
//    private Consumer<String, SpecificRecordBase> consumer;
    private final CollectorConfig config;

    public KafaClientImp(@Autowired CollectorConfig config) {
        this.config = config;
    }

    public Producer getProducer() {
        if (producer == null)
            initProducer();

        return producer;
    }

//    public Consumer getConsumer() {
//        return null;
//    }

    public void stop() {
        if (producer != null) {
            // отправляем оставшиеся данные и закрываем продюсер
            producer.flush();
            producer.close(Duration.ofSeconds(10));
        }

//        if (consumer != null)
//            consumer.close();
    }

    private void initProducer() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafka().getMain().getServerConfig());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "ru.yandex.practicum.kafka.telemetry.serialization.SensorAvroSerializer");
        producer = new KafkaProducer<>(properties);
    }
}
