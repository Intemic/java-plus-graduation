package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
public abstract class BaseProcessor<K, V> {
    protected Consumer<K, V> consumer;
    private final KafkaConfig.KafkaConsumerConfig config;
    protected Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public BaseProcessor(String serverConfig, KafkaConfig.KafkaConsumerConfig consumerConfig) {
        config = consumerConfig;
        consumer = initConsumer(serverConfig, config);
    }

    private Consumer<K, V> initConsumer(String serverConfig, KafkaConfig.KafkaConsumerConfig consumerConfig) {
        log.info("Создаем консьюмер");
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverConfig);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerConfig.getKeyDeserializer());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerConfig.getValueDeserializer());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerConfig.getGroupId());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerConfig.getAutoOffsetReset());
        return new KafkaConsumer<>(properties);
    }

    public void stop() {
        log.info("Закрываем консьюмер");
        consumer.close();
    }

    public void start() {
        consumer.subscribe(List.of(config.getTopic()));
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            while (true) {
                ConsumerRecords<K, V> records = consumer.poll(config.getDurationMillis());
                for (ConsumerRecord<K, V> record : records) {
                    process(record);
                    fixOffset(record);
                }

                fixCommit();
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки", e);
        } finally {
            try {
                finishFixCommit();
            } finally {
                stop();
            }
        }
    }

    public void finishFixCommit() {
        if (currentOffsets.isEmpty())
            consumer.commitSync();
        else
            consumer.commitSync(currentOffsets);
    }


    public abstract void process(ConsumerRecord<K, V> record);

    public abstract void fixOffset(ConsumerRecord<K, V> record);

    public abstract void fixCommit();

}

