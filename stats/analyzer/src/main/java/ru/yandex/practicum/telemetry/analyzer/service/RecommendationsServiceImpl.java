package ru.yandex.practicum.telemetry.analyzer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.yandex.practicum.telemetry.analyzer.model.Interaction;
import ru.yandex.practicum.telemetry.analyzer.model.Similaritie;
import ru.yandex.practicum.telemetry.analyzer.repository.InteractionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SimilaritieRepository;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationsServiceImpl implements RecommendationsService {
    private final InteractionRepository interactionRepository;
    private final SimilaritieRepository similaritieRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        List<RecommendedEventProto> listEvents = List.of();

        log.info("Пришло событие - %s".formatted(convertToJson(request)));

        // ищем мероприятия с какими взаимодействовал пользователь
        Map<Long, Interaction> interactionMap = interactionRepository.findAllByUserId(request.getUserId()).stream()
                .collect(Collectors.toMap(Interaction::getEventId, Function.identity()));
        if (!interactionMap.isEmpty()) {
            // перечень всех мероприятий с которыми взаимодействовал
            List<Long> allEventIds = interactionMap.values().stream()
                    .map(Interaction::getEventId)
                    .toList();

            // данные событий с которыми взаимодействовал
            Map<Long, Map<Long, Double>> similaritieMap = similaritieRepository.
                    findAllByEventAInOrEventBIn(allEventIds, allEventIds).stream()
                    .collect(Collectors.toMap(Similaritie::getEventA,
                            similaritie -> {
                                Map<Long, Double> innerMap = new HashMap<>();
                                innerMap.put(similaritie.getEventB(), similaritie.getScore());
                                return innerMap;
                            },
                            (existingMap, newMap) -> {
                                existingMap.putAll(newMap);
                                return existingMap;
                            }));

            // последние n мероприятий с которыми взаимодействовал пользователь
            List<Long> latestEventIds = interactionMap.values().stream()
                    .sorted(Collections.reverseOrder(Comparator.comparing(Interaction::getTimeStamp)))
                    .limit(request.getMaxResults())
                    .map(Interaction::getEventId)
                    .toList();

            // новые мероприятия с которыми еще не взаимодействовал
            List<Long> newEventsIds = latestEventIds.stream()
                    .flatMap(eventId -> {
                        if (!similaritieMap.containsKey(eventId)) {
                            return Stream.of(eventId);  // возвращаем само событие, если нет похожих
                        }
                        return similaritieMap.get(eventId).entrySet().stream()
                                .filter(entry -> !allEventIds.contains(entry.getKey()))
                                .map(Map.Entry::getKey);
                    })
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, Map<Long, Double>> similaritieNewMap = similaritieRepository.
                    findAllByEventAInOrEventBIn(newEventsIds, newEventsIds).stream()
                    .collect(Collectors.toMap(similaritie ->
                                    newEventsIds.contains(similaritie.getEventA()) ? similaritie.getEventA()
                                            : similaritie.getEventB(),
                            similaritie -> {
                                Map<Long, Double> innerMap = new HashMap<>();
                                innerMap.put(newEventsIds.contains(similaritie.getEventA()) ? similaritie.getEventB()
                                        : similaritie.getEventA(), similaritie.getScore());
                                return innerMap;
                            },
                            (existingMap, newMap) -> {
                                existingMap.putAll(newMap);
                                return existingMap;
                            }));

            similaritieNewMap.entrySet().stream().map(entry -> {
                        double numerator = entry.getValue().entrySet().stream()
                                // учитываем только отмеченные пользователем значения
                                .filter(entryInner -> interactionMap.containsKey(entryInner.getKey()))
                                .map(entryInner ->
                                        entryInner.getValue() * interactionMap.get(entryInner.getKey()).getRating())
                                .mapToDouble(Double::doubleValue)
                                .sum();

                        double denominator = entry.getValue().entrySet().stream()
                                // учитываем только отмеченные пользователем значения
                                .filter(entryInner -> interactionMap.containsKey(entryInner.getKey()))
                                .map(entryInner ->
                                        entryInner.getValue())
                                .mapToDouble(Double::doubleValue)
                                .sum();

                        double result = 0.0;
                        if (denominator > 0.0)
                            denominator = numerator / denominator;

                        return RecommendedEventProto.newBuilder()
                                .setEventId(entry.getKey())
                                .setScore(denominator)
                                .build();

                    })
                    .sorted(Collections.reverseOrder(Comparator.comparing(RecommendedEventProto::getScore)))
                    .limit(request.getMaxResults())
                    .toList();
        }

        process(listEvents, responseObserver);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        List<RecommendedEventProto> listEvents = List.of();

        log.info("Пришло событие - %s".formatted(convertToJson(request)));

        Map<Long, Double> mapEvents = similaritieRepository
                .findAllByEventAOrEventB(request.getEventId(), request.getEventId()).stream()
                .collect(Collectors.toMap(similaritie ->
                                similaritie.getEventA()
                                        // ищем отличное от входящего событие
                                        .equals(request.getEventId()) ? similaritie.getEventB() : similaritie.getEventA(),
                        Similaritie::getScore));

        if (!mapEvents.isEmpty()) {
            // получили похожие события которые пользователь еще не отметил
            List<Long> evetnsId = interactionRepository
                    .findAllByEventIdInAndUserIdNot(mapEvents.keySet(), request.getUserId()).stream()
                    .map(Interaction::getEventId)
                    .toList();
            listEvents = evetnsId.stream()
                    .map(evetnId -> RecommendedEventProto.newBuilder()
                            .setEventId(evetnId)
                            .setScore(mapEvents.get(evetnsId))
                            .build())
                    .sorted(Collections.reverseOrder(Comparator.comparing(RecommendedEventProto::getScore)))
                    .limit(request.getMaxResults())
                    .toList();
        }

        process(listEvents, responseObserver);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Пришло событие - %s".formatted(convertToJson(request)));

        Map<Long, Double> mapEventSumRaiting = interactionRepository.findAllByEventIdIn(request.getEventIdList()).stream()
                .collect(Collectors.toMap(Interaction::getEventId, Interaction::getRating,
                        Double::sum));
        List<RecommendedEventProto> listEvents = mapEventSumRaiting.entrySet().stream()
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .toList();

        process(listEvents, responseObserver);
    }

    private void process(List<RecommendedEventProto> listEvents,
                         StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Отправляем %s событий:".formatted(listEvents.size()));

        // после обработки события возвращаем ответ клиенту
        listEvents.stream().forEach(event -> {
            log.info(convertToJson(event));
            responseObserver.onNext(event);
        });
        // и завершаем обработку запроса
        responseObserver.onCompleted();
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return object.toString();
        }
    }

    private List<RecommendedEventProto> getSimilarEventsInner(long eventId, long userId, int maxResult) {
        List<RecommendedEventProto> listEvents = List.of();

        Map<Long, Double> mapEvents = similaritieRepository
                .findAllByEventAOrEventB(eventId, eventId).stream()
                .collect(Collectors.toMap(similaritie ->
                                similaritie.getEventA()
                                        // ищем отличное от входящего событие
                                        .equals(eventId) ? similaritie.getEventB() : similaritie.getEventA(),
                        Similaritie::getScore));

        if (!mapEvents.isEmpty()) {
            // получили похожие события которые пользователь еще не отметил
            List<Long> evetnsId = interactionRepository
                    .findAllByEventIdInAndUserIdNot(mapEvents.keySet(), userId).stream()
                    .map(Interaction::getEventId)
                    .toList();
            listEvents = evetnsId.stream()
                    .map(evetnId -> RecommendedEventProto.newBuilder()
                            .setEventId(evetnId)
                            .setScore(mapEvents.get(evetnsId))
                            .build())
                    .sorted(Collections.reverseOrder(Comparator.comparing(RecommendedEventProto::getScore)))
                    .limit(maxResult)
                    .toList();
        }

        return listEvents;
    }

}
