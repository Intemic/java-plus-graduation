package ru.practicum.stats.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GrpcUserActionApp {
    public static void main(String[] args) {
        SpringApplication.run(GrpcUserActionApp.class, args);
    }
}
