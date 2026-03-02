package ru.yandex.practicum.telemetry.analyzer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("analyzer.kafka")
public class KafkaConfig {
    private String serverConfig;
    private KafkaConsumersConfig consumers;

    @Getter
    @Setter
    public static class KafkaConsumersConfig {
        private KafkaConsumerConfig userActions;
        private KafkaConsumerConfig eventsSimilarity;
    }

    @Getter
    @Setter
    public static class KafkaConsumerConfig {
        private String keyDeserializer;
        private String valueDeserializer;
        private String groupId;
        private int durationMillis;
        private String autoOffsetReset;
        private String topic;
    }

}
