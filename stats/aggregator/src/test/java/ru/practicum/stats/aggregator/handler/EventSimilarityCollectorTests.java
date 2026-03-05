package ru.practicum.stats.aggregator.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class EventSimilarityCollectorTests {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private EventSimilarityCollector eventCollector;
    private List<UserActionAvro> list;

    @BeforeEach
    public void beforeEach() throws IOException {
        Resource resource = new ClassPathResource("test.txt");
        String filePath = resource.getFile().getAbsolutePath();

        //String filePath = "test.txt";
        list = new LinkedList<>();
        eventCollector = new EventSimilarityCollector(objectMapper);

        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.forEach(line -> {
                try {
                    JsonNode node = objectMapper.readTree(line);
                    list.add(UserActionAvro.newBuilder()
                            .setUserId(node.get("user").asLong())
                            .setEventId(node.get("event").asLong())
                            .setActionType(ActionTypeAvro.valueOf(node.get("type").asText().split("_")[1]))
                            .setTimestamp(Instant.parse(node.get("timestamp").asText()))
                            .build());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void startTest() {
        list.stream().forEach(event -> eventCollector.updateState(event));
    }
}
