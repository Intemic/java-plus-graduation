package ru.practicum.stats.aggregator.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class EventSimilarityCollector {
    // ключи: Event, User, Weight
    private Map<Long, Map<Long, Double>> mapMaxEventUserWeight = new HashMap<>();
    // ключи Event, Event, Weight
    private Map<Long, Map<Long, Double>> mapMinEventEventWeight = new HashMap<>();
    private final ObjectMapper objectMapper;

    public EventSimilarityCollector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<EventSimilarityAvro> updateState(UserActionAvro event) {
        try {
            log.info("Пришло сообщение %s".formatted(objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            log.info("Пришло сообщение %s".formatted(event.toString()));
        }

        if (needUpdating(event))
            return getEventsSimilarityAvro(event);

        log.info("Данные события не изменились");
        return List.of();
    }

    private boolean needUpdating(UserActionAvro event) {
        Double currentWeight =  getWeightForAction(event.getActionType());
        Double oldWeight = null;

        // если вес пришедшего действия меньше, ничего не делаем
        if (mapMaxEventUserWeight.containsKey(event.getEventId())) {
            Map<Long, Double> mapUserWeight = mapMaxEventUserWeight.get(event.getEventId());
            if (mapUserWeight.containsKey(event.getUserId())) {
                oldWeight = mapUserWeight.get(event.getUserId());
                log.info("Сохраненое значение веса: %s, пришло значение: %s".formatted(oldWeight, currentWeight));
                if (oldWeight >= currentWeight) {
                    log.info("Обновление не требуется");
                    return false;
                }
            }
        }

        log.info("Сохраненое значение веса: %s, пришло значение: %s"
                .formatted(oldWeight == null ? "null" : oldWeight, currentWeight));
        log.info("Данные необходимо обновить");
        return true;
    }

    private double getWeightForAction(ActionTypeAvro action) {
        return switch (action) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

//    public void put(long eventA, long eventB, double sum) {
//        long first  = Math.min(eventA, eventB);
//        long second = Math.max(eventA, eventB);
//
//        minWeightsSums
//                .computeIfAbsent(first, e -> new HashMap<>())
//                .put(second, sum);
//    }
//
//    public double get(long eventA, long eventB) {
//        long first  = Math.min(eventA, eventB);
//        long second = Math.max(eventA, eventB);
//
//        return minWeightsSums
//                .computeIfAbsent(first, e -> new HashMap<>())
//                .getOrDefault(second, 0.0);
//    }

    private List<EventSimilarityAvro> getEventsSimilarityAvro(UserActionAvro event) {

//        return  EventSimilarityAvro.newBuilder()
//                .setEventA()
//                .setEventB()
//                .setScore()
//                .setTimestamp(Instant.now())
//                .build();
        return List.of();
    }
}
