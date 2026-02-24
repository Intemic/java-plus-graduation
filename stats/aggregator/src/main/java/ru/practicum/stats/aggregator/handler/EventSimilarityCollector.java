package ru.practicum.stats.aggregator.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Optional;

@Slf4j
public class EventSimilarityCollector {
//    private Map<String, SensorsSnapshotAvro> shaphots = new HashMap<>();
    //private Map<Long, Map<>>
    private final ObjectMapper objectMapper;

    public EventSimilarityCollector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<EventSimilarityAvro> updateState(UserActionAvro event) {
        try {
            log.info("Пришло сообщение %s".formatted(objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            log.info("Пришло сообщение %s".formatted(event.toString()));
        }

//        SensorsSnapshotAvro snapshotAvro = shaphots.computeIfAbsent(event.getHubId(), k -> SensorsSnapshotAvro.newBuilder()
//                .setHubId(event.getHubId())
//                .setTimestamp(event.getTimestamp())
//                .setSensorsState(new HashMap<>())
//                .build());
//
//        if (needUpdating(event, snapshotAvro.getSensorsState().get(event.getId()))) {
//            snapshotAvro.getSensorsState().put(event.getId(), SensorStateAvro.newBuilder()
//                    .setTimestamp(event.getTimestamp())
//                    .setData(event.getPayload())
//                    .build());
//            snapshotAvro.setTimestamp(event.getTimestamp());
//
//            return Optional.of(snapshotAvro);
//        }

        log.info("Данные события не изменились");
        return Optional.empty();
    }

//    private boolean needUpdating(SensorEventAvro event, SensorStateAvro sensorsState) {
//        if (sensorsState == null) {
//            log.info("Отличаются значение, предыдущего события нет");
//            return true;
//        }
//
//        // если событие произошло позже и данные изменились
//        if (event.getTimestamp().isAfter(sensorsState.getTimestamp())
//                || event.getTimestamp().equals(sensorsState.getTimestamp())) {
//            SpecificRecordBase payload = (SpecificRecordBase) event.getPayload();
//            SpecificRecordBase data = (SpecificRecordBase) sensorsState.getData();
//            Schema schema = payload.getSchema();
//
//            // сравниваем по полям
//            for (Schema.Field field : schema.getFields()) {
//                Object newValue = payload.get(field.name());
//                Object oldValue = data.get(field.name());
//                if (!newValue.equals(oldValue)) {
//                    log.info("Отличаются значения поля: \"%s\", старое = %s, новое = %s".formatted(
//                            field.name(),
//                            oldValue == null ? "null" : oldValue.toString(),
//                            newValue.toString())
//                    );
//                    return true;
//                }
//            }
//        }
//
 //        return false;
//    }

}
