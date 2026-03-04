package ru.practicum.client;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Service
public class UserActionClient {
    @GrpcClient("collector")
    UserActionControllerGrpc.UserActionControllerBlockingStub userActionController;

    public void collectUserAction(long userId, long eventId, ActionTypeProto actionType) {
        Instant instant = Instant.now();

        UserActionProto userAction = UserActionProto.newBuilder()
                .setUserId(userId)
                .setUserId(eventId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds( instant.getEpochSecond())
                        .setNanos(instant.getNano())
                        .build())
                .build();

        userActionController.collectUserAction(userAction);
    }
}
