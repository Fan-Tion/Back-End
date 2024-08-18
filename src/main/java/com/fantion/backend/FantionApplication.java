package com.fantion.backend;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(
    servers = {
        @Server(url="https://www.fantion.kro.kr", description = "Deployment Server url"),
        @Server(url="http://localhost:8080", description = "local Server url")
    }
)
@EnableFeignClients
@EnableScheduling
@SpringBootApplication
public class FantionApplication {
  public static void main(String[] args) {
    SpringApplication.run(FantionApplication.class, args);
  }
}
