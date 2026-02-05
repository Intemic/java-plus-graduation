package ru.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.StatsClient;

@Configuration
public class MainServiceConfig {
    @Value("${stats-client.id:stats-server}")
    private String statsClientId;

    @Bean
    public StatsClient getStatClient(DiscoveryClient discoveryClient) {
        return new StatsClient(statsClientId, discoveryClient);
    }
}