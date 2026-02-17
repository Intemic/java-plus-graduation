package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@SpringBootApplication(scanBasePackages = {
//        "ru.practicum.core.interaction.api",
//        "ru.practicum.category",
//        "ru.practicum.comment",
//        "ru.practicum.compilation",
//        "ru.practicum.config",
//        "ru.practicum.event",
//        "ru.practicum.exception"
//})
@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.core.interaction.api.client")
public class MainServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(MainServiceApp.class, args);
    }
}
