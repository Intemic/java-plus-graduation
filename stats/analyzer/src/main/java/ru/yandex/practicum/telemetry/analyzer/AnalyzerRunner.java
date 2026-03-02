package ru.yandex.practicum.telemetry.analyzer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.analyzer.processor.EventsSimilarityProcessor;
import ru.yandex.practicum.telemetry.analyzer.processor.HubEventProcessor;
import ru.yandex.practicum.telemetry.analyzer.processor.SnapshotProcessor;
import ru.yandex.practicum.telemetry.analyzer.processor.UserActionProcessor;

@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {
    private final UserActionProcessor userActionProcessor;
    private final EventsSimilarityProcessor eventsSimilarityProcessor;

    @Override
    public void run(String... args) throws Exception {
        Thread userThread = new Thread(userActionProcessor);
        userThread.start();

        eventsSimilarityProcessor.start();
    }
}
