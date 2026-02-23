package ru.practicum.stats.collector.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.stats.collector.config.CollectorConfig;
import ru.practicum.stats.collector.config.KafkaClient;

import java.time.Instant;

import static ru.practicum.ewm.stats.avro.ActionTypeAvro.*;

@Slf4j
@Service
public class UserActionServiceImpl implements UserActionService{
    private final String topic;
    private final Producer<Long, SpecificRecordBase> producer;
    private final KafkaClient kafkaClient;

    public UserActionServiceImpl(KafkaClient kafkaClient,
                                 CollectorConfig config) {
        this.topic = config.getKafka().getTopics().getAction();
        this.producer = kafkaClient.getProducer();
        this.kafkaClient = kafkaClient;
    }

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        // отправляем в Kafka
        sendMessage(request);
        // после обработки события возвращаем ответ клиенту
        responseObserver.onNext(Empty.getDefaultInstance());
        // и завершаем обработку запроса
        responseObserver.onCompleted();
    }

    private void sendMessage(UserActionProto request) {
        log.info("Пришло сообщение: %s".formatted(request.toString()));
        UserActionAvro actionAvro = UserActionAvro.newBuilder()
                .setUserId(request.getUserId())
                .setEventId(request.getEventId())
                .setActionType(switch(request.getActionType()) {
                    case ACTION_VIEW -> VIEW;
                    case ACTION_REGISTER -> REGISTER;
                    case ACTION_LIKE -> LIKE;
                    case UNRECOGNIZED -> null;
                })
                .setTimestamp(Instant.ofEpochSecond(request.getTimestamp().getSeconds(),
                        request.getTimestamp().getNanos()))
                .build();

        log.info("Значение для отправки - %s".formatted(actionAvro.toString()));
        ProducerRecord<Long, SpecificRecordBase> record =
                new ProducerRecord<>(topic, actionAvro);
        producer.send(record);
        log.info("Отправлено в kafka");
    }

    @PreDestroy
    public void stop() {
        kafkaClient.stop();
    }
}
