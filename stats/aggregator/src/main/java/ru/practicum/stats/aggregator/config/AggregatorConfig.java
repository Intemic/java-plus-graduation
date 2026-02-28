package ru.practicum.stats.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
//@ConfigurationProperties("aggregator")
public class AggregatorConfig {
    private KafkaConfig kafka;

    @Getter
    @Setter
    public static class KafkaConfig {
        private MainKafkaConfig main;
        private TopicKafkaConfig topics;
    }

    @Getter
    @Setter
    public static class MainKafkaConfig {
        private String serverConfig;
        private MainKafkaProducerConfig producer;
        private MainKafkaConsumerConfig consumer;
    }

    @Getter
    @Setter
    public static class MainKafkaProducerConfig {
        private String keySerializer;
        private String valueSerializer;
    }

    @Getter
    @Setter
    public static class MainKafkaConsumerConfig {
        private String keyDeserializer;
        private String valueDeserializer;
        private String groupId;
        private int durationMillis;
        private String autoOffsetReset;
    }

    @Getter
    @Setter
    public static class TopicKafkaConfig {
        private String action;
        private String similarity;
    }
}
