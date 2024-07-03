//package com.fantion.backend.config;
//
//import io.tossgrpc.Server;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class TossGrpcServerRunner implements ApplicationRunner, DisposableBean {
//
//    private final Server tossgrpcServer;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        tossgrpcServer.start();
//        tossgrpcServer.awaitTermination();
//    }
//
//    @Override
//    public void destroy() {
//        if (tossgrpcServer != null) {
//            tossgrpcServer.shutdown();
//        }
//    }
//}