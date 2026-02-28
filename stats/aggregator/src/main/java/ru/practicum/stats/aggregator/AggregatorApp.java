package ru.practicum.stats.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.stats.aggregator.handler.AggregationStarter;

import java.io.IOException;

@SpringBootApplication
//@ConfigurationPropertiesScan
public class AggregatorApp {
    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AggregatorApp.class, args);
        //context.getBean(AggregationStarter.class).start();
        context.getBean(AggregationStarter.class).startTest();
    }
}
