package ru.practicum.stats.aggregator.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.aggregator.config.AggregatorConfig;
import ru.practicum.stats.aggregator.kafka.KafkaClientImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private static final int MAX_COUNT_PROCESSED_RECORDS = 10;
    //private final AggregatorConfig config;
    private AggregatorConfig config;
    //private final KafkaClientImpl kafkaClient;
    private KafkaClientImpl kafkaClient;
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
        eventCollector.updateState(record.value())
                .forEach(eventSimilarity -> {

                    String json;
                    try {
                        json = objectMapper.writeValueAsString(eventSimilarity);
                    } catch (JsonProcessingException e) {
                        json = eventSimilarity.toString();
                    }

                    ProducerRecord<String, EventSimilarityAvro> recordAvro =
                            new ProducerRecord<>(config.getKafka().getTopics().getSimilarity(), eventSimilarity);
                    kafkaClient.getProducer().send(recordAvro);
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

    public void startTest() throws IOException {
        String filePath = "test.txt";
        List<UserActionAvro> list = new LinkedList<>();
        eventCollector = new EventSimilarityCollector(objectMapper);

        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.forEach(line -> {
                try {
                    JsonNode node = objectMapper.readTree(line);
//
//                    String user = node.get("user").asText();
//                    String event = node.get("event").asText();
//                    String type = ;
//                    String timestamp = node.get("timestamp").asText();
//
//                    Long userId = ();
//                    ActionTypeAvro actionType = ActionTypeAvro.valueOf(type);
//

                    list.add(UserActionAvro.newBuilder()
//                            .setUserId(node.get("user").longValue())
//                            .setEventId(node.get("event").longValue())
//                            .setActionType(actionType)
//                            .setTimestamp(Instant.parse(timestamp))
                            .setUserId(node.get("user").asLong())
                            .setEventId(node.get("event").asLong())
                            .setActionType(ActionTypeAvro.valueOf(node.get("type").asText().split("_")[1]))
                            .setTimestamp(Instant.parse(node.get("timestamp").asText()))
                            .build());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });

            list.stream().forEach(event -> eventCollector.updateState(event));
        }
    }
}
