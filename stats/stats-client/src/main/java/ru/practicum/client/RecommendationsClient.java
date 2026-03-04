package ru.practicum.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class RecommendationsClient {
    @GrpcClient("analyzer")
    RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationsController;

   public List<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
       UserPredictionsRequestProto requestProto = UserPredictionsRequestProto.newBuilder()
               .setUserId(userId)
               .setMaxResults(maxResults)
               .build();

       return convertToList(recommendationsController.getRecommendationsForUser(requestProto));
   }

   public List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
       SimilarEventsRequestProto requestProto = SimilarEventsRequestProto.newBuilder()
               .setEventId(eventId)
               .setUserId(userId)
               .setMaxResults(maxResults)
               .build();

       return convertToList(recommendationsController.getSimilarEvents(requestProto));
   }

    public List<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        if (eventIds.isEmpty())
            return List.of();

        InteractionsCountRequestProto requestProto = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();

       return convertToList(recommendationsController.getInteractionsCount(requestProto));
    }

    private List<RecommendedEventProto> convertToList(Iterator<RecommendedEventProto> iterator) {
        List<RecommendedEventProto> listEvents = new ArrayList<>();
        iterator.forEachRemaining(listEvents::add);
        return listEvents;
    }
}
