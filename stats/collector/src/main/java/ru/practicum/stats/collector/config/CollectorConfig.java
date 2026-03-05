package ru.practicum.stats.collector.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("collector")
public class CollectorConfig {
    private KafkaConfig kafka;

    @Getter
    @Setter
    public static class KafkaConfig {
        private MainKafkaConfig main;
        private TopicKafkaConfig topics;
        private SerializerClass serializerClass;
    }

    @Getter
    @Setter
    public static class MainKafkaConfig {
        private String serverConfig;
    }

    @Getter
    @Setter
    public static  class TopicKafkaConfig {
        private String action;
    }

    @Getter
    @Setter
    public static  class SerializerClass {
        private String key;
        private String value;
    }
}
