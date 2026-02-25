package ru.practicum.stats.aggregator.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import static java.lang.Math.min;
import static java.lang.Math.max;


import java.time.Instant;
import java.util.*;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class EventSimilarityCollector {
    // перечень всех обработанных событий
    private TreeSet<Long> setEvents = new TreeSet<>();
    // ключи: Event, User, Weight, максимальный вес действия пользователя
    private Map<Long, Map<Long, Double>> mapMaxEventUserWeight = new HashMap<>();
    // ключи Event, Event, Weight, минимальный коэфф двух событий
    private Map<Long, Map<Long, Double>> mapMinEventEventWeight = new HashMap<>();
    // ключи Event, Double коэфф для события
    private Map<Long, Double> mapEventSum = new HashMap<>();
    // ключи Event, Event, Double, коэфф похожести событий
    private Map<Long, Map<Long, Double>> mapEventEventScope = new HashMap<>();

    private final ObjectMapper objectMapper;

    public EventSimilarityCollector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EventSimilarityCollector(TreeSet<Long> setEvents, Map<Long, Map<Long, Double>> mapMaxEventUserWeight, Map<Long, Map<Long, Double>> mapMinEventEventWeight, ObjectMapper objectMapper) {
        this.setEvents = setEvents;
        this.mapMaxEventUserWeight = mapMaxEventUserWeight;
        this.mapMinEventEventWeight = mapMinEventEventWeight;
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
        Double currentWeight = getWeightForAction(event.getActionType());
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
        double weight = getWeightForAction(event.getActionType());
        double oldWeight;

        // получаем преведущую оценку
        oldWeight = mapMaxEventUserWeight.computeIfAbsent(event.getEventId(), v -> new HashMap<>())
                .computeIfAbsent(event.getUserId(), v -> Double.valueOf(0));
//        // получаем преведущую оценку
//        oldWeight = mapMaxEventUserWeight.computeIfAbsent(event.getEventId(), v -> new HashMap<>())
//                .computeIfAbsent(event.getUserId(), v -> Double.valueOf(0));
//
//        // разница весов
//        deltaWeight = newWeight - oldWeight;


        // новое не учтенное событие, пересчитываем
//        if (!setEvents.contains(event.getEventId())) {
//            mapEventSum.put(event.getEventId(), weight);
        // пересчитываем коэфф для всех существующих событий
        List<EventSimilarityAvro> eventList = setEvents.stream()
                .filter(eventId -> eventId != event.getEventId())
                .map(eventId -> calculateScore(eventId, event))
                .filter(Optional::isPresent)
                .map(optionalEventEventScope -> {
                    Map<Long, Map<Long, Double>> mapEventEventScope = optionalEventEventScope.get();
                    Long eventMin = mapEventEventScope.keySet().stream().findFirst().get();
                    Long eventMax = mapEventEventScope.get(eventMin).keySet().stream().findFirst().get();

                    return EventSimilarityAvro.newBuilder()
                            .setEventA(eventMin)
                            .setEventB(eventMax)
                            .setScore(mapEventEventScope.get(eventMin).get(eventMax))
                            .setTimestamp(Instant.now())
                            .build();
                })
                .toList();
//        } else {
//
//        }


        mapMaxEventUserWeight.get(event.getEventId()).compute(event.getUserId(), (k, v) -> weight);
        setEvents.add(event.getEventId());

//        return  EventSimilarityAvro.newBuilder()
//                .setEventA()
//                .setEventB()
//                .setScore()
//                .setTimestamp(Instant.now())
//                .build();


        return eventList;
    }

    private Optional<Map<Long, Map<Long, Double>>> calculateScore(long eventIdOne, UserActionAvro event) {
        double newWeight = getWeightForAction(event.getActionType());

        long eventMin = Math.min(eventIdOne, event.getEventId());
        long eventMax = Math.max(eventIdOne, event.getEventId());

        // преведущие максимальные веса оценок событий(Smin)
        double weigthEventMin = mapMaxEventUserWeight.computeIfAbsent(eventMin, v -> new HashMap<>())
                .computeIfAbsent(event.getUserId(), v -> 0.0);

        double weigthEventMax = mapMaxEventUserWeight.computeIfAbsent(eventMax, v -> new HashMap<>())
                .computeIfAbsent(event.getUserId(), v -> 0.0);

        // минимальный вес старых оценок
        double minOldWeigth = Math.min(weigthEventMin, weigthEventMax);

        // минимальный вес новых оценок
        double minNewWeigth;
        if (event.getEventId() == eventMin)
            minNewWeigth = min(newWeight, weigthEventMin);
        else
            minNewWeigth = min(newWeight, weigthEventMax);

        double deltaMinWeigth = minNewWeigth - minOldWeigth;

        // создадим, если нет
        Map<Long, Double> mapEventWeigth = mapMinEventEventWeight.computeIfAbsent(eventMin, v -> new HashMap<>());
        mapEventWeigth.computeIfAbsent(eventMax, v -> 0.0);
        // обновим общую сумму минимальных весов
        mapMinEventEventWeight.get(eventMin).compute(eventMax, (k, v) -> v + deltaMinWeigth);

        // пересчитаем сумму весов
        double sumOldMinEventWeigth = mapEventSum.computeIfAbsent(eventMin, v -> 0.0);
        double sumOldMaxEventWeigth = mapEventSum.computeIfAbsent(eventMax, v -> 0.0);

        double deltaMaxWeigth;
        if (event.getEventId() == eventMin) {
            deltaMaxWeigth = newWeight - weigthEventMin;
            sumOldMinEventWeigth = mapEventSum.compute(eventMin, (k, v) -> v + deltaMaxWeigth);
        } else {
            deltaMaxWeigth = newWeight - weigthEventMin;
            sumOldMaxEventWeigth = mapEventSum.compute(eventMax, (k, v) -> v + deltaMaxWeigth);
        }

        // занесем если есть
        mapEventEventScope.computeIfAbsent(eventMin, v -> new HashMap<>())
                .computeIfAbsent(eventMax, k -> 0.0);

        // пересчитаем коэффициенты если есть изменения
        if (deltaMinWeigth != 0 || deltaMaxWeigth != 0) {
            if (sumOldMinEventWeigth != 0 && sumOldMaxEventWeigth != 0) {
                double scope = minNewWeigth / ((Math.sqrt(sumOldMinEventWeigth) * Math.sqrt(sumOldMaxEventWeigth)));
                mapEventEventScope.get(eventMin).compute(eventMax, (k, v) -> scope);
                return Optional.of(Map.of(eventMin, mapEventEventScope.get(eventMin)));
            }
        }

        return Optional.empty();
    }
}
