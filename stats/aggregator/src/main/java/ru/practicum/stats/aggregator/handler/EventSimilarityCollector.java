package ru.practicum.stats.aggregator.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;


import java.time.Instant;
import java.util.*;

@Slf4j
public class EventSimilarityCollector {
    // ключи: Event, User, Weight, максимальный вес действия пользователя
    private final Map<Long, Map<Long, Double>> mapEventUserMaxWeight = new HashMap<>();
    // ключи Event, Event, Weight, минимальный коэфф двух событий
    private final Map<Long, Map<Long, Double>> mapEventEventMinWeight = new HashMap<>();
    // ключи Event, Double коэфф для события
    private final Map<Long, Double> mapEventSum = new HashMap<>();

    private final ObjectMapper objectMapper;

    public EventSimilarityCollector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<EventSimilarityAvro> updateState(UserActionAvro event) {
        List<EventSimilarityAvro> eventList = List.of();

        log.info("");
        log.info("<===================================start=====================================>");

        try {
            log.info("Пришло сообщение %s".formatted(objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            log.info("Пришло сообщение %s".formatted(event.toString()));
        }

        if (needUpdating(event)) {
            eventList = getEventsSimilarityAvro(event);
//            updateStatistic(event);
        } else {
            log.info("Данные события не изменились");
        }

        log.info("<===================================end=====================================>");

        return eventList;
    }

    private boolean needUpdating(UserActionAvro event) {
        double oldWeight = 0;
        double currentWeight = getWeightForAction(event.getActionType());

        Optional<Map.Entry<Long, Map<Long, Double>>> eventUser = mapEventUserMaxWeight.entrySet().stream()
                .filter(entry -> entry.getKey().equals(event.getEventId())
                        && entry.getValue().containsKey(event.getUserId()))
                .findFirst();

        if (eventUser.isPresent())
            oldWeight = eventUser.get().getValue().get(event.getUserId());


        // обновляем только если вес пришедшего действия больше, либо еще не было значения
        if (eventUser.isEmpty() || (oldWeight < currentWeight)) {
            // обновляем только если вес пришедшего действия больше
            log.info("Старое значение веса: %s, пришло значение: %s, данные необходимо обновить"
                    .formatted(oldWeight, currentWeight));
            return true;
        }

        log.info("Старое значение веса: %s, пришло значение: %s, обновление не требуется"
                .formatted(oldWeight, currentWeight));
        return false;
    }

    private double getWeightForAction(ActionTypeAvro action) {
        return switch (action) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    private List<EventSimilarityAvro> getEventsSimilarityAvro(UserActionAvro event) {
        List<EventSimilarityAvro> eventList = List.of();


        // новое не учтенное событие, пересчитываем
        if (!mapEventUserMaxWeight.containsKey(event.getEventId())) {
            Set<Long> eventIds = Set.copyOf(mapEventUserMaxWeight.keySet());
            if (eventIds.isEmpty())
                log.info("Новое событие, отсутствуют события для пересчета");
            else
                log.info("Новое событие, старт пересчета схожести событий");

            mapEventUserMaxWeight.computeIfAbsent(event.getEventId(), k -> new HashMap<>())
                    .compute(event.getUserId(), (userId, oldWeight) ->
                            getWeightForAction(event.getActionType()));

            eventList = eventIds.stream()
                    .map(eventId ->
                            createEventSimilarity(eventId,
                                    event.getEventId(), calculateScope(eventId, event.getEventId())))
                    .filter(Objects::nonNull)
                    .toList();

            // обновим данные
        } else {

            log.info("Пересчет похожести событий:");

            // ищем старое значение для события
            double oldMaxAvroWeight;
            if (mapEventUserMaxWeight.containsKey(event.getEventId())
                    && mapEventUserMaxWeight.get(event.getEventId()).containsKey(event.getUserId()))
                oldMaxAvroWeight = mapEventUserMaxWeight.get(event.getEventId()).get(event.getUserId());
            else
                oldMaxAvroWeight = 0.0;

            eventList = mapEventUserMaxWeight.entrySet().stream() //.toList() setEvents.stream()
                    .filter(entry -> !entry.getKey().equals(event.getEventId())
                            // пользователь должен был совершить действие с сравниванимым событием
                            && mapEventUserMaxWeight.get(entry.getKey()).containsKey(event.getUserId()))
                    .map(entry ->
                            createEventSimilarity(entry.getKey(),
                                    event.getEventId(),
                                    recalculateScope(entry, event, oldMaxAvroWeight))
                    )
                    .filter(Objects::nonNull)
                    .toList();

            // обновим
            mapEventUserMaxWeight.get(event.getEventId())
                    .compute(event.getUserId(), (userId, weight) ->
                            getWeightForAction(event.getActionType()));

            mapEventSum.compute(event.getEventId(),
                    (k, v) ->
                            round(v + (getWeightForAction(event.getActionType()) - oldMaxAvroWeight), 2));


        }

        return eventList;
    }

    private double calculateSX(long eventId) {
        double weigth;

        if (mapEventUserMaxWeight.containsKey(eventId))
            weigth = round(mapEventUserMaxWeight.get(eventId).values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum(), 2);
        else {
            weigth = 0.0;
        }

        mapEventSum.computeIfAbsent(eventId, k -> 0.0);
        mapEventSum.compute(eventId, (k, v) -> weigth);

        return weigth;
    }

    private double calculateSmin(long eventIdOne, long eventIdTwo) {
        long eventMin = Math.min(eventIdOne, eventIdTwo);
        long eventMax = Math.max(eventIdOne, eventIdTwo);

        double smin = round(mapEventUserMaxWeight.get(eventMin).entrySet().stream()
                .mapToDouble(entry ->
                        // ищем оценку события eventIdTwo для этого же пользователя
                        min(entry.getValue(),
                                mapEventUserMaxWeight.getOrDefault(eventMax, Map.of(entry.getKey(), 0.0))
                                        .getOrDefault(entry.getKey(), 0.0))
                )
                .sum(), 2);

        // сохраним расчет
        mapEventEventMinWeight.computeIfAbsent(eventMin, k -> new HashMap<>())
                .put(eventMax, 0.0);
        mapEventEventMinWeight.get(eventMin).compute(eventMax, (k, v) -> smin);

        return smin;
    }

    private double calculateScope(long eventIdOne, long eventIdTwo) {
        String message;
        double scope = 0.0;
        double smin = 0.0;

        log.info("Расчитываем коэффициент похожести для событий %s, %s".formatted(eventIdOne, eventIdTwo));

        double sOne = calculateSX(eventIdOne);
        double sTwo = calculateSX(eventIdTwo);

        message = "Суммарный коэффициент для событий: %s - [%s], %s - [%s]"
                .formatted(eventIdOne, sOne, eventIdTwo, sTwo);

        if (sOne != 0.0 && sTwo != 0.0) {
            smin = calculateSmin(eventIdOne, eventIdTwo);
            scope = round(smin / (sqrt(sOne) * sqrt(sTwo)),
                    2);
        }

        log.info(message + ", Smin - [%s], коэффициент похожести = [%s]".formatted(smin, scope));
        return scope;
    }

//    private void updateStatistic(UserActionAvro event) {
//        // сохраним полученные значения
//        mapEventUserMaxWeight.computeIfAbsent(event.getEventId(), v -> new HashMap<>())
//                .computeIfAbsent(event.getUserId(), v -> 0.0);
//
//        mapEventUserMaxWeight.get(event.getEventId())
//                .compute(event.getUserId(), (k, v) -> getWeightForAction(event.getActionType()));
////        setEvents.add(event.getEventId());
//    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private EventSimilarityAvro createEventSimilarity(long eventIdOne, long eventIdTwo, double scope) {
        // нет смысла
        if (scope == 0.0)
            return null;

        return EventSimilarityAvro.newBuilder()
                .setEventA(min(eventIdOne, eventIdTwo))
                .setEventB(max(eventIdOne, eventIdTwo))
                .setScore(scope)
                .setTimestamp(Instant.now())
                .build();
    }

    private double recalculateScope(Map.Entry<Long, Map<Long, Double>> eventComp,
                                    UserActionAvro userAction,
                                    double oldMaxAvroWeight) {
        String message;
        double scope = 0.0;

        long eventMin = min(eventComp.getKey(), userAction.getEventId());
        long eventMax = max(eventComp.getKey(), userAction.getEventId());

        log.info("Расчитываем коэффициент похожести для событий %s, %s".formatted(eventMin, eventMax));

        // знчение сравниваемого события
        double maxCompWeight = eventComp.getValue().get(userAction.getUserId());
        log.info("Максимальны вес для события %s - [%s]".formatted(eventComp.getKey(), maxCompWeight));

        // значение пришедшего события
        double newMaxAvroWeight = getWeightForAction(userAction.getActionType());

        double deltaMaxWeight = newMaxAvroWeight - oldMaxAvroWeight;

        double sOne = mapEventSum.get(eventComp.getKey());
        double sTwo = mapEventSum.get(userAction.getEventId());
        double smin = mapEventEventMinWeight.get(eventMin).get(eventMax);
        log.info("Суммарный старый коэффициент для событий: %s - [%s], %s - [%s], Smin - [%s]"
                .formatted(eventMin, sOne, eventMax, sTwo, smin));

        sTwo = sTwo + deltaMaxWeight;

        double deltaSminWeight = min(newMaxAvroWeight, maxCompWeight) - min(oldMaxAvroWeight, maxCompWeight);
        log.info("Разница Smin старого и нового значения - %s".formatted(deltaSminWeight));

        message = "Суммарный коэффициент для событий: %s - [%s], %s - [%s]"
                .formatted(eventMin, sOne, eventMax, sTwo);

        smin = mapEventEventMinWeight.get(eventMin).compute(eventMax, (k, v) -> v + deltaSminWeight);

        // пересчитаем похожесть
        //double sOne = mapEventSum.get(eventComp.getKey());
        scope = round(smin / (sqrt(sOne) * sqrt(sTwo)), 2);
        log.info(message + ", Smin - [%s], коэффициент похожести = [%s]".formatted(round(smin, 2), scope));

        return scope;
    }
}
