package com.fantion.backend.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableCaching
@EnableDiscoveryClient
@EnableSwagger2
@SpringBootApplication
public class TossPaymentService {

    public static void main(String[] args) {
        SpringApplication.run(TossPaymentService.class, args);
    }

}