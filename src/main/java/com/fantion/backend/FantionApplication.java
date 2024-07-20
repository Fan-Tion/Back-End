package com.fantion.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class FantionApplication {
  public static void main(String[] args) {
    SpringApplication.run(FantionApplication.class, args);
  }
}
