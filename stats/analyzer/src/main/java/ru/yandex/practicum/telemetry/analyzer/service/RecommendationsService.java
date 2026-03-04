package ru.yandex.practicum.telemetry.analyzer.service;

import io.grpc.stub.StreamObserver;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

public interface RecommendationsService {
   void getRecommendationsForUser(UserPredictionsRequestProto request,
                              StreamObserver<RecommendedEventProto> responseObserver);

   void getSimilarEvents(SimilarEventsRequestProto request,
                     StreamObserver<RecommendedEventProto> responseObserver);

   void getInteractionsCount(InteractionsCountRequestProto request,
                         StreamObserver<RecommendedEventProto> responseObserver);
}
