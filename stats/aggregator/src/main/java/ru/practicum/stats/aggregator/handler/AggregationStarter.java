package ru.practicum.stats.aggregator.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.aggregator.config.AggregatorConfig;
import ru.practicum.stats.aggregator.kafka.KafkaClientImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private static final int MAX_COUNT_PROCESSED_RECORDS = 10;
    private final AggregatorConfig config;
    private final KafkaClientImpl kafkaClient;
    private final ObjectMapper objectMapper;
    private EventSimilarityCollector eventCollector;
    private Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private int processedRecord;

    public void start() {
        eventCollector = new EventSimilarityCollector(objectMapper);
        processedRecord = 0;
        Consumer<String, UserActionAvro> consumer = kafkaClient.getConsumer();
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(config.getKafka().getTopics().getAction()));

            // Цикл обработки событий
            while (true) {
                ConsumerRecords<String, UserActionAvro> records =
                        consumer.poll(config.getKafka().getMain().getConsumer().getDurationMillis());

                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    process(record);
                    fixOffset(record, consumer);
                }
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                // Перед тем, как закрыть продюсер и консьюмер, нужно убедиться,
                // что все сообщения, лежащие в буфере, отправлены и
                // все офсеты обработанных сообщений зафиксированы
                kafkaClient.getProducer().flush();
                // здесь нужно вызвать метод продюсера для сброса данных в буфере
                // здесь нужно вызвать метод консьюмера для фиксации смещений
                consumer.commitSync(currentOffsets);
            } finally {
                kafkaClient.stop();
            }
        }
    }

    private void process(ConsumerRecord<String, UserActionAvro> record) {
        // данные изменились? отправляем
        eventCollector.updateState(record.value()).ifPresent((snapshotAvro) -> {
            ProducerRecord<String, EventSimilarityAvro> recordAvro =
                    new ProducerRecord<>(config.getKafka().getTopics().getSimilarity(), snapshotAvro);
            kafkaClient.getProducer().send(recordAvro);
            String json;
            try {
                json = objectMapper.writeValueAsString(snapshotAvro);
            } catch (JsonProcessingException e) {
                json = snapshotAvro.toString();
            }
            log.info("Отправлено сообщение в Kafka: %s".formatted(json));
        });
    }

    private void fixOffset(ConsumerRecord<String, UserActionAvro> record,
                           Consumer<String, UserActionAvro> consumer) {
        currentOffsets.put(new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1));

        if (processedRecord % MAX_COUNT_PROCESSED_RECORDS == 0)
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });

        processedRecord++;
    }
}
