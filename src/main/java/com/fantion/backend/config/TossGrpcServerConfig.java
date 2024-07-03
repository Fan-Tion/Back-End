package com.fantion.backend.config;

import com.fantion.backend.payment.tossgrpc.TossPaymentServiceImpl;
import io.tossgrpc.Server;
import io.tossgrpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TossGrpcServerConfig {

    @Value("${grpc.server.port}")
    private int grpcServerPort;

    @Bean
    public Server grpcServer(TossPaymentServiceImpl paymentService) {
        return ServerBuilder.forPort(grpcServerPort)
                .addService(paymentService).build();
    }
}