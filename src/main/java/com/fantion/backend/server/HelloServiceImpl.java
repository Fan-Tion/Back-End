package com.fantion.backend.server;


import io.grpc.stub.StreamObserver;

public class HelloServiceImpl extends com.fantion.backend.payment.tossgrpc.TossGreeterGrpc.GreeterImplBase{

	@Override
	public void sayHello(com.fantion.backend.payment.tossgrpc.TossHelloRequest request, StreamObserver<com.fantion.backend.payment.tossgrpc.TossHelloReply> responseObserver) {
		
		/**
		 * 예) DataBase 테이블에서 값을 조회한다.
		 **/
		String getAddress = "";
		
		
		String sendName = request.getName();
		
		System.out.println("=====================================================");
		System.out.println("요청이 옴.");
		System.out.println("=====================================================");
		System.out.println("request : "+request);
		System.out.println("요청 유저명 : " + sendName);
		
		
		com.fantion.backend.payment.tossgrpc.TossHelloReply helloReply = com.fantion.backend.payment.tossgrpc.TossHelloReply.newBuilder()
				.setMessage("User's Address Value : "+getAddress)
				.build();
		
		responseObserver.onNext(helloReply);
		responseObserver.onCompleted();
		
		System.out.println("=====================================================");
		System.out.println("GRPC 호출이 종료 됨.");
		System.out.println("=====================================================");
	}
	
	
	
}
