//package com.fantion.backend.payment.tossgrpc;
//
//import com.fantion.backend.payment.tossgrpc.GreeterGrpc;
//
//import io.grpc.stub.StreamObserver;
//
//public class HelloServiceImpl extends GreeterGrpc.GreeterImplBase{
//    @Override
//    public void sayHello(com.grpc.TossHelloRequest request, StreamObserver<com.grpc.TossHelloReply> responseObserver) {
//        /**
//         * 예) DataBase 테이블에서 값을 조회한다.
//         **/
//        String getAddress = "서울 강남구 역삼동 110 번지";
//
//        String sendName = request.getName();
//
//        System.out.println("=====================================================");
//        System.out.println("요청이 옴.");
//        System.out.println("=====================================================");
//        System.out.println("request : "+request);
//        System.out.println("요청 유저명 : " + sendName);
//
//
//        com.grpc.TossHelloReply helloReply = com.grpc.TossHelloReply.newBuilder()
//                .setMessage("User's Address Value : "+getAddress)
//                .build();
//
//        responseObserver.onNext(helloReply);
//        responseObserver.onCompleted();
//
//        System.out.println("=====================================================");
//        System.out.println("GRPC 호출이 종료 됨.");
//        System.out.println("=====================================================");
//    }
//}
//출처: https://ecolumbus.tistory.com/139 [슬기로운 개발자 생활:티스토리]