//package com.fantion.backend.client;
//
//import com.fantion.backend.GreeterGrpc;
//import com.fantion.backend.HelloReply;
//import com.fantion.backend.HelloRequest;
//
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//
//public class HelloGrpcClient {
//
//	public static void main(String[] args) {
//
//		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
//				.usePlaintext()
//				.build();
//
//		GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
//
//		HelloReply helloReply = blockingStub.sayHello(HelloRequest.newBuilder()
//				.setName("ksm")
//				.build());
//
//		System.out.println("*****************************************************");
//		System.out.println("요청 결과가 옴.");
//		System.out.println("response : " + helloReply);
//		System.out.println("");
//		System.out.println("response : " + helloReply.getMessage());
//		System.out.println("*****************************************************");
//
//		channel.isShutdown();
//
//	}
//
//}
