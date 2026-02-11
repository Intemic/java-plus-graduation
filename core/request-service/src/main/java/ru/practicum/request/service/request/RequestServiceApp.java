package ru.practicum.request.service.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.user.repository",
        "ru.practicum.request.service.request" })
//@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.core.interaction.api.client")
public class RequestServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApp.class, args);
    }
}
