package ru.practicum.event.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class EventServiceConfig {
    @Value("${stats-client.id:stats-server}")
    private String statsClientId;

    private FixedBackOffPolicy fixedBackOffPolicy(long backOffPeriod) {
        FixedBackOffPolicy policy = new FixedBackOffPolicy();
        policy.setBackOffPeriod(backOffPeriod);
        return policy;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(3)
                .customBackoff(fixedBackOffPolicy(2000L))
                .build();
    }

}