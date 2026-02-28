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
import java.util.concurrent.RecursiveTask;
import java.util.stream.Stream;

@Slf4j
public class EventSimilarityCollector {
    // перечень всех обработанных событий
 //   private TreeSet<Long> setEvents = new TreeSet<>();
    // ключи: User, Event, Weight, максимальный вес действия пользователя
    //  private Map<Long, Map<Long, Double>> mapUserEventMaxWeight = new HashMap<>();
    //    // ключи: Event, User, Weight, максимальный вес действия пользователя
    private Map<Long, Map<Long, Double>> mapEventUserMaxWeight = new HashMap<>();
    // ключи Event, Event, Weight, минимальный коэфф двух событий
    private Map<Long, Map<Long, Double>> mapEventEventMinWeight = new HashMap<>();
    // ключи Event, Double коэфф для события
    private Map<Long, Double> mapEventSum = new HashMap<>();
    // ключи Event, Event, Double, коэфф похожести событий
    //private Map<Long, Map<Long, Double>> mapEventEventScope = new HashMap<>();

    private final ObjectMapper objectMapper;

    public EventSimilarityCollector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<EventSimilarityAvro> updateState(UserActionAvro event) {
        List<EventSimilarityAvro> eventList = List.of();

        try {
            log.info("Пришло сообщение %s".formatted(objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            log.info("Пришло сообщение %s".formatted(event.toString()));
        }

        if (needUpdating(event)) {
            eventList = getEventsSimilarityAvro(event);
            updateStatistic(event);
        } else {
            log.info("Данные события не изменились");
        }

        return eventList;
    }

    private boolean needUpdating(UserActionAvro event) {
        double oldWeight = 0;
        double currentWeight = getWeightForAction(event.getActionType());

        Optional<Map.Entry<Long, Map<Long, Double>>> eventUser = mapEventUserMaxWeight.entrySet().stream()
                .filter(entry -> entry.getKey().equals(event.getEventId())
                        && entry.getValue().containsKey(event.getUserId()))
                .findFirst();

//        Stream<Map.Entry<Long, Map<Long, Double>>> stream = mapEventUserMaxWeight.entrySet().stream();
//        Optional<Map.Entry<Long, Map<Long, Double>>> eventUser = stream.filter(entry -> entry.getKey().equals(event.getEventId())
//                && entry.getValue().containsKey(event.getUserId()))
//                        .findFirst();




        if (eventUser.isPresent())
            oldWeight = eventUser.get().getValue().get(event.getUserId());


        // обновляем только если вес пришедшего действия больше, либо еще не было значения
        if (eventUser.isEmpty() || (oldWeight < currentWeight)) {
            // обновляем только если вес пришедшего действия больше
            log.info("Старое значение веса: %s, пришло значение: %s".formatted(oldWeight, currentWeight));
            log.info("Данные необходимо обновить");
            return true;
        }

        return false;
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
        List<EventSimilarityAvro> eventList = List.of();

        log.info("Старт пересчета коэффициентов для событий");

        // новое не учтенное событие, пересчитываем
//        if (!setEvents.contains(event.getEventId())) {
//            mapEventSum.put(event.getEventId(), weight);
        // пересчитываем коэфф для всех существующих событий
//        List<EventSimilarityAvro> eventList = setEvents.stream()
//        eventList = setEvents.stream()
//                .filter(eventId -> eventId != event.getEventId())
//                // пользователь должен был оценить данное мероприятие
//                .filter(eventId -> mapUserEventMaxWeight.containsKey(event.getUserId())
//                        && mapUserEventMaxWeight.get(event.getUserId()).containsKey(eventId)
//                )
//                .map(eventId -> calculateScore(eventId, event))
//                .filter(Optional::isPresent)
//                .map(optionalEventEventScope -> {
//                    Map<Long, Map<Long, Double>> mapEventEventScope = optionalEventEventScope.get();
//                    Long eventMin = mapEventEventScope.keySet().stream().findFirst().get();
//                    Long eventMax = mapEventEventScope.get(eventMin).keySet().stream().findFirst().get();
//
//                    return EventSimilarityAvro.newBuilder()
//                            .setEventA(eventMin)
//                            .setEventB(eventMax)
//                            .setScore(mapEventEventScope.get(eventMin).get(eventMax))
//                            .setTimestamp(Instant.now())
//                            .build();
//                })
//                .toList();

        // новое не учтенное событие, пересчитываем
        if (!mapEventUserMaxWeight.containsKey(event.getEventId())) {
          Set<Long> eventIds = mapEventUserMaxWeight.keySet();
//
//             сохраним полученные значения
//            mapEventUserMaxWeight.computeIfAbsent(event.getEventId(), v -> new HashMap<>())
//                    .computeIfAbsent(event.getUserId(), v -> 0.0);
//
//            mapEventUserMaxWeight.get(event.getEventId())
//                    .compute(event.getUserId(), (k, v) -> getWeightForAction(event.getActionType()));

            mapEventUserMaxWeight.computeIfAbsent(event.getEventId(), k -> new HashMap<>())
                    .compute(event.getUserId(), (userId, oldWeight) ->
                            getWeightForAction(event.getActionType()));

            eventList = eventIds.stream() //setEvents.stream()
//                    .filter(eventId -> !eventId.equals(event.getEventId()))
//                    .filter(eventId -> !eventId.equals(event.getEventId())
//                            // пользователь должен был совершить действие со сравниванимым событием
//                            && mapEventUserMaxWeight.get(eventId).containsKey(event.getUserId()))
                    .map(eventId -> {
                        log.info("Сравиваем события %s и %s".formatted(eventId, event.getEventId()));
                        return createEventSimilarity(eventId, event.getEventId(), calculateScope(eventId, event.getEventId()));
                    })
                    .filter(Objects::nonNull)
                    .toList();

//            // обновим данные
        } else {

            // ищем старое значение для события
            double oldMaxAvroWeight = mapEventUserMaxWeight.get(event.getEventId()).get(event.getUserId());
            double newMaxAvroWeight = getWeightForAction(event.getActionType());
            double deltaMaxWeight = newMaxAvroWeight - oldMaxAvroWeight;

            eventList = mapEventUserMaxWeight.entrySet().stream() //.toList() setEvents.stream()
                    .filter(eventId -> !eventId.equals(event.getEventId())
                            // пользователь должен был совершить действие с сравниванимым событием
                            && mapEventUserMaxWeight.get(eventId).containsKey(event.getUserId()))
                    .map(eventId -> {
                        double scope = recalculateScope(eventId, event);

                        if (scope == 0.0)
                            return null;

                        return createEventSimilarity(eventId, event.getEventId(), scope);
                    })
                    .filter(Objects::nonNull)
                    .toList();

        }

        return eventList;
    }

//    private Optional<Map.Entry<Long, Map.Entry<Long, Double>>> calculateScore(long eventIdOne, UserActionAvro event) {
//        double newWeight = getWeightForAction(event.getActionType());
//
//        long eventMin = Math.min(eventIdOne, event.getEventId());
//        long eventMax = Math.max(eventIdOne, event.getEventId());
//
//        // преведущие максимальные веса оценок событий(Smin)
//        double weigthEventMin = mapEventUserMaxWeight.computeIfAbsent(eventMin, v -> new HashMap<>())
//                .computeIfAbsent(event.getUserId(), v -> 0.0);
//
//        double weigthEventMax = mapEventUserMaxWeight.computeIfAbsent(eventMax, v -> new HashMap<>())
//                .computeIfAbsent(event.getUserId(), v -> 0.0);
//
//        // минимальный вес старых оценок
//        double minOldWeigth = Math.min(weigthEventMin, weigthEventMax);
//
//        // минимальный вес новых оценок
//        double minNewWeigth;
//        if (event.getEventId() == eventMin)
//            minNewWeigth = min(newWeight, weigthEventMin);
//        else
//            minNewWeigth = min(newWeight, weigthEventMax);
//
//        double deltaMinWeigth = minNewWeigth - minOldWeigth;
//
//        // создадим, если нет
//        Map<Long, Double> mapEventWeigth = mapEventEventMinWeight.computeIfAbsent(eventMin, v -> new HashMap<>());
//        mapEventWeigth.computeIfAbsent(eventMax, v -> 0.0);
//        // обновим общую сумму минимальных весов
//        mapEventEventMinWeight.get(eventMin).compute(eventMax, (k, v) -> v + deltaMinWeigth);
//
//        // пересчитаем сумму весов
//        double sumOldMinEventWeigth = mapEventSum.computeIfAbsent(eventMin, v -> 0.0);
//        double sumOldMaxEventWeigth = mapEventSum.computeIfAbsent(eventMax, v -> 0.0);
//
//        double deltaMaxWeigth;
//        if (event.getEventId() == eventMin) {
//            deltaMaxWeigth = newWeight - weigthEventMin;
//            sumOldMinEventWeigth = mapEventSum.compute(eventMin, (k, v) -> v + deltaMaxWeigth);
//        } else {
//            deltaMaxWeigth = newWeight - weigthEventMin;
//            sumOldMaxEventWeigth = mapEventSum.compute(eventMax, (k, v) -> v + deltaMaxWeigth);
//        }
//
//        // занесем если есть
//        mapEventEventScope.computeIfAbsent(eventMin, v -> new HashMap<>())
//                .computeIfAbsent(eventMax, k -> 0.0);
//
//        // пересчитаем коэффициенты если есть изменения
//        if (deltaMinWeigth != 0.0 || deltaMaxWeigth != 0.0) {
//            if (sumOldMinEventWeigth != 0 && sumOldMaxEventWeigth != 0) {
//                double scope = round(
//                        minNewWeigth / ((Math.sqrt(sumOldMinEventWeigth) * Math.sqrt(sumOldMaxEventWeigth))),
//                        2);
//                mapEventEventScope.get(eventMin).compute(eventMax, (k, v) -> scope);
//                //if (scope > 0)
//                return Optional.of(Map.of(eventMin, mapEventEventScope.get(eventMin)));
//            }
//        }
//
//        return Optional.empty();
//    }

    private double calculateSX(long eventId) {
        double weigth;

        if (mapEventUserMaxWeight.containsKey(eventId))
            weigth = round( mapEventUserMaxWeight.get(eventId).values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum(), 1);
        else {
            weigth = 0.0;
        }

        mapEventSum.computeIfAbsent(eventId, k -> 0.0);
        mapEventSum.compute(eventId, (k, v) -> weigth);

        log.info("Суммарный коэффициент для события %s = [%s]".formatted(eventId, weigth));
        return weigth;
    }

    private double calculateSmin(long eventIdOne, long eventIdTwo) {
        long eventMin = Math.min(eventIdOne, eventIdTwo);
        long eventMax = Math.max(eventIdOne, eventIdTwo);

        double smin = mapEventUserMaxWeight.get(eventMin).entrySet().stream()
                .mapToDouble(entry ->
                        // ищем оценку события eventIdTwo для этого же пользователя
                        min(entry.getValue(),
                                mapEventUserMaxWeight.getOrDefault(eventMax, Map.of(entry.getKey(), 0.0))
                                        .getOrDefault(entry.getKey(), 0.0))
                )
                .sum();

        log.info("Минимальный коэффициент для событий %s и %s = [%s]".formatted(eventIdOne, eventIdTwo, smin));

        // сохраним расчет
        mapEventEventMinWeight.computeIfAbsent(eventMin, k -> new HashMap<>())
                .put(eventMax, 0.0);
        mapEventEventMinWeight.get(eventMin).compute(eventMax, (k, v) -> smin);

        return smin;
    }

    private double calculateScope(long eventIdOne, long eventIdTwo) {
        log.info("Расчитываем коэффициент похожести для событий %s, %s".formatted(eventIdOne, eventIdTwo));

        double sOne = calculateSX(eventIdOne);
        double sTwo = calculateSX(eventIdTwo);

        if (sOne == 0.0 || sTwo == 0.0)
            return 0.0;

        double scope = round(calculateSmin(eventIdOne, eventIdTwo) / (sqrt(sOne) * sqrt(sTwo)),
                2);

        log.info("Коэффициент похожести для событий событий %s, %s - [%s]".formatted(eventIdOne, eventIdTwo, scope));

        return scope;
    }

    private void updateStatistic(UserActionAvro event) {
        // сохраним полученные значения
        mapEventUserMaxWeight.computeIfAbsent(event.getEventId(), v -> new HashMap<>())
                .computeIfAbsent(event.getUserId(), v -> 0.0);

        mapEventUserMaxWeight.get(event.getEventId())
                .compute(event.getUserId(), (k, v) -> getWeightForAction(event.getActionType()));
//        setEvents.add(event.getEventId());
    }

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

    private double recalculateScope(long eventId, UserActionAvro event) {
        long eventMin = min(eventId, event.getEventId());
        long eventMax = max(eventId, event.getEventId());

        // ищем старое значение для события
        double oldMaxAvroWeight = mapEventUserMaxWeight.get(event.getEventId()).get(event.getUserId());
        double newMaxAvroWeight = getWeightForAction(event.getActionType());
        double deltaMaxWeight = newMaxAvroWeight - oldMaxAvroWeight;
//       // есть изменения, обновим коэффициент
//       if (deltaMaxWeight != 0.0)
//           mapEventSum.compute(event.getEventId(), (k, v) -> v + deltaMaxWeight);

        double oldMaxEventWeight = mapEventUserMaxWeight.get(eventId).get(event.getUserId());
        // delta Smin старое
        double oldSminWeight = min(oldMaxAvroWeight, oldMaxEventWeight);
        double newSminWeight = min(newMaxAvroWeight, oldMaxEventWeight);
        double deltaSminWeight = newSminWeight - oldSminWeight;

        // пересчитываем Smin
        if (deltaSminWeight != 0.0)
            mapEventEventMinWeight.get(eventMin).compute(eventMax, (k, v) -> v + deltaSminWeight);

        // пересчитаем вес
        if (deltaMaxWeight != 0.0 || deltaSminWeight != 0.0) {
            double smin = mapEventEventMinWeight.get(eventMin).get(eventMax);
            double sOne = mapEventSum.get(eventId);
            double sTwo = mapEventSum.get(event.getEventId()) + deltaMaxWeight;

            return round(smin / (sqrt(sOne) * sqrt(sTwo)), 2);
        }


        return null;
        //return mapEventEventScope.get(eventMin).get(eventMax);
    }
}
