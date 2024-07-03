package com.fantion.backend.server;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class HelloGrpcServer {

	public static void main(String[] args) throws InterruptedException, IOException{
		
		Server grpcServer = ServerBuilder
				.forPort(8080)
				.addService(new HelloServiceImpl()).build();
		
		grpcServer.start();
		grpcServer.awaitTermination();
				
	}

}
