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
import java.util.stream.Collectors;

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

        // ищем с какими мероприятиями взаимодействовал в последнее время пользователь
        List<Interaction> interactionList = interactionRepository
                .findAllByUserIdOrderByTimeStampDesc(request.getUserId(),
                        PageRequest.of(1, request.getMaxResults()));
        if (!interactionList.isEmpty()) {


            // получили похожие события которые пользователь еще не отметил
            List<Long> evetnsId = interactionRepository
                    .findAllByEventIdInAndUserIdNot(mapEvents.keySet(), request.getUserId()).stream()
                    .map(Interaction::getEventId)
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
            responseObserver.onNext(event); });
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



}
