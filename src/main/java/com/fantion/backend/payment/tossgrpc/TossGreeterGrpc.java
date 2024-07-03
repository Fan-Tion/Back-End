//package com.fantion.backend.payment.tossgrpc;
//
//import static io.grpc.MethodDescriptor.generateFullMethodName;
//import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
//import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
//import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
//import static io.grpc.stub.ClientCalls.asyncUnaryCall;
//import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
//import static io.grpc.stub.ClientCalls.blockingUnaryCall;
//import static io.grpc.stub.ClientCalls.futureUnaryCall;
//import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
//import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
//import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
//import static io.grpc.stub.ServerCalls.asyncUnaryCall;
//import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
//import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
//
///**
// */
//@javax.annotation.Generated(
//    value = "by gRPC proto compiler (version 1.15.0)",
//    comments = "Source: hello.proto")
//public final class TossGreeterGrpc {
//
//  private TossGreeterGrpc() {}
//
//  public static final String SERVICE_NAME = "helloworld.Greeter";
//
//  // Static method descriptors that strictly reflect the proto.
//  private static volatile io.grpc.MethodDescriptor<com.fantion.backend.payment.tossgrpc.TossHelloRequest,
//          com.fantion.backend.payment.tossgrpc.TossHelloReply> getSayHelloMethod;
//
//  @io.grpc.stub.annotations.RpcMethod(
//      fullMethodName = SERVICE_NAME + '/' + "SayHello",
//      requestType = TossHelloRequest.class,
//      responseType = TossHelloReply.class,
//      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
//  public static io.grpc.MethodDescriptor<TossHelloRequest,
//          TossHelloReply> getSayHelloMethod() {
//    io.grpc.MethodDescriptor<TossHelloRequest, TossHelloReply> getSayHelloMethod;
//    if ((getSayHelloMethod = com.fantion.backend.payment.grpc.TossGreeterGrpc.getSayHelloMethod) == null) {
//      synchronized (TossGreeterGrpc.class) {
//        if ((getSayHelloMethod = com.fantion.backend.payment.grpc.TossGreeterGrpc.getSayHelloMethod) == null) {
//          com.fantion.backend.payment.grpc.TossGreeterGrpc.getSayHelloMethod = getSayHelloMethod =
//              io.grpc.MethodDescriptor.<TossHelloRequest, TossHelloReply>newBuilder()
//              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
//              .setFullMethodName(generateFullMethodName(
//                  "helloworld.Greeter", "SayHello"))
//              .setSampledToLocalTracing(true)
//              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
//                  com.fantion.backend.payment.grpc.TossHelloRequest.getDefaultInstance()))
//              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
//                  com.fantion.backend.payment.grpc.TossHelloReply.getDefaultInstance()))
//                  .setSchemaDescriptor(new GreeterMethodDescriptorSupplier("SayHello"))
//                  .build();
//          }
//        }
//     }
//     return getSayHelloMethod;
//  }
//
//  private static volatile io.tossgrpc.MethodDescriptor<com.fantion.backend.payment.tossgrpc.TossHelloRequest,
//          com.fantion.backend.payment.tossgrpc.TossHelloReply> getSayHelloAgainMethod;
//
//  @io.grpc.stub.annotations.RpcMethod(
//      fullMethodName = SERVICE_NAME + '/' + "SayHelloAgain",
//      requestType = TossHelloRequest.class,
//      responseType = TossHelloReply.class,
//      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
//  public static io.grpc.MethodDescriptor<TossHelloRequest,
//          TossHelloReply> getSayHelloAgainMethod() {
//    io.grpc.MethodDescriptor<TossHelloRequest, TossHelloReply> getSayHelloAgainMethod;
//    if ((getSayHelloAgainMethod = com.fantion.backend.payment.grpc.TossGreeterGrpc.getSayHelloAgainMethod) == null) {
//      synchronized (TossGreeterGrpc.class) {
//        if ((getSayHelloAgainMethod = com.fantion.backend.payment.grpc.TossGreeterGrpc.getSayHelloAgainMethod) == null) {
//          com.fantion.backend.payment.grpc.TossGreeterGrpc.getSayHelloAgainMethod = getSayHelloAgainMethod =
//              io.grpc.MethodDescriptor.<TossHelloRequest, TossHelloReply>newBuilder()
//              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
//              .setFullMethodName(generateFullMethodName(
//                  "helloworld.Greeter", "SayHelloAgain"))
//              .setSampledToLocalTracing(true)
//              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
//                  com.fantion.backend.payment.grpc.TossHelloRequest.getDefaultInstance()))
//              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
//                  com.fantion.backend.payment.grpc.TossHelloReply.getDefaultInstance()))
//                  .setSchemaDescriptor(new GreeterMethodDescriptorSupplier("SayHelloAgain"))
//                  .build();
//          }
//        }
//     }
//     return getSayHelloAgainMethod;
//  }
//
//  /**
//   * Creates a new async stub that supports all call types for the service
//   */
//  public static GreeterStub newStub(io.grpc.Channel channel) {
//    return new GreeterStub(channel);
//  }
//
//  /**
//   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
//   */
//  public static GreeterBlockingStub newBlockingStub(
//      io.grpc.Channel channel) {
//    return new GreeterBlockingStub(channel);
//  }
//
//  /**
//   * Creates a new ListenableFuture-style stub that supports unary calls on the service
//   */
//  public static GreeterFutureStub newFutureStub(
//      io.grpc.Channel channel) {
//    return new GreeterFutureStub(channel);
//  }
//
//  /**
//   */
//  public static abstract class GreeterImplBase implements io.grpc.BindableService {
//
//    /**
//     */
//    public void sayHello(com.fantion.backend.payment.tossgrpc.TossHelloRequest request,
//                         io.grpc.stub.StreamObserver<com.fantion.backend.payment.tossgrpc.TossHelloReply> responseObserver) {
//      asyncUnimplementedUnaryCall(getSayHelloMethod(), responseObserver);
//    }
//
//    /**
//     */
//    public void sayHelloAgain(com.fantion.backend.payment.tossgrpc.TossHelloRequest request,
//                              io.grpc.stub.StreamObserver<com.fantion.backend.payment.tossgrpc.TossHelloReply> responseObserver) {
//      asyncUnimplementedUnaryCall(getSayHelloAgainMethod(), responseObserver);
//    }
//
//    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
//      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
//          .addMethod(
//            getSayHelloMethod(),
//            asyncUnaryCall(
//              new MethodHandlers<
//                      com.fantion.backend.payment.tossgrpc.TossHelloRequest,
//                      com.fantion.backend.payment.tossgrpc.TossHelloReply>(
//                  this, METHODID_SAY_HELLO)))
//          .addMethod(
//            getSayHelloAgainMethod(),
//            asyncUnaryCall(
//              new MethodHandlers<
//                      com.fantion.backend.payment.tossgrpc.TossHelloRequest,
//                      com.fantion.backend.payment.tossgrpc.TossHelloReply>(
//                  this, METHODID_SAY_HELLO_AGAIN)))
//          .build();
//    }
//  }
//
//  /**
//   */
//  public static final class GreeterStub extends io.grpc.stub.AbstractStub<GreeterStub> {
//    private GreeterStub(io.grpc.Channel channel) {
//      super(channel);
//    }
//
//    private GreeterStub(io.grpc.Channel channel,
//        io.grpc.CallOptions callOptions) {
//      super(channel, callOptions);
//    }
//
//    @java.lang.Override
//    protected GreeterStub build(io.grpc.Channel channel,
//        io.grpc.CallOptions callOptions) {
//      return new GreeterStub(channel, callOptions);
//    }
//
//    /**
//     */
//    public void sayHello(com.fantion.backend.payment.tossgrpc.TossHelloRequest request,
//                         io.grpc.stub.StreamObserver<com.fantion.backend.payment.tossgrpc.TossHelloReply> responseObserver) {
//      asyncUnaryCall(
//          getChannel().newCall(getSayHelloMethod(), getCallOptions()), request, responseObserver);
//    }
//
//    /**
//     */
//    public void sayHelloAgain(com.fantion.backend.payment.tossgrpc.TossHelloRequest request,
//                              io.grpc.stub.StreamObserver<com.fantion.backend.payment.tossgrpc.TossHelloReply> responseObserver) {
//      asyncUnaryCall(
//          getChannel().newCall(getSayHelloAgainMethod(), getCallOptions()), request, responseObserver);
//    }
//  }
//
//  /**
//   */
//  public static final class GreeterBlockingStub extends io.grpc.stub.AbstractStub<GreeterBlockingStub> {
//    private GreeterBlockingStub(io.grpc.Channel channel) {
//      super(channel);
//    }
//
//    private GreeterBlockingStub(io.grpc.Channel channel,
//        io.grpc.CallOptions callOptions) {
//      super(channel, callOptions);
//    }
//
//    @java.lang.Override
//    protected GreeterBlockingStub build(io.grpc.Channel channel,
//        io.grpc.CallOptions callOptions) {
//      return new GreeterBlockingStub(channel, callOptions);
//    }
//
//    /**
//     */
//    public com.fantion.backend.payment.tossgrpc.TossHelloReply sayHello(com.fantion.backend.payment.tossgrpc.TossHelloRequest request) {
//      return blockingUnaryCall(
//          getChannel(), getSayHelloMethod(), getCallOptions(), request);
//    }
//
//    /**
//     */
//    public com.fantion.backend.payment.tossgrpc.TossHelloReply sayHelloAgain(com.fantion.backend.payment.tossgrpc.TossHelloRequest request) {
//      return blockingUnaryCall(
//          getChannel(), getSayHelloAgainMethod(), getCallOptions(), request);
//    }
//  }
//
//  /**
//   */
//  public static final class GreeterFutureStub extends io.grpc.stub.AbstractStub<GreeterFutureStub> {
//    private GreeterFutureStub(io.grpc.Channel channel) {
//      super(channel);
//    }
//
//    private GreeterFutureStub(io.grpc.Channel channel,
//        io.grpc.CallOptions callOptions) {
//      super(channel, callOptions);
//    }
//
//    @java.lang.Override
//    protected GreeterFutureStub build(io.grpc.Channel channel,
//        io.grpc.CallOptions callOptions) {
//      return new GreeterFutureStub(channel, callOptions);
//    }
//
//    /**
//     */
//    public com.google.common.util.concurrent.ListenableFuture<com.fantion.backend.payment.tossgrpc.TossHelloReply> sayHello(
//        com.fantion.backend.payment.tossgrpc.TossHelloRequest request) {
//      return futureUnaryCall(
//          getChannel().newCall(getSayHelloMethod(), getCallOptions()), request);
//    }
//
//    /**
//     */
//    public com.google.common.util.concurrent.ListenableFuture<com.fantion.backend.payment.tossgrpc.TossHelloReply> sayHelloAgain(
//        com.fantion.backend.payment.tossgrpc.TossHelloRequest request) {
//      return futureUnaryCall(
//          getChannel().newCall(getSayHelloAgainMethod(), getCallOptions()), request);
//    }
//  }
//
//  private static final int METHODID_SAY_HELLO = 0;
//  private static final int METHODID_SAY_HELLO_AGAIN = 1;
//
//  private static final class MethodHandlers<Req, Resp> implements
//      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
//      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
//      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
//      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
//    private final GreeterImplBase serviceImpl;
//    private final int methodId;
//
//    MethodHandlers(GreeterImplBase serviceImpl, int methodId) {
//      this.serviceImpl = serviceImpl;
//      this.methodId = methodId;
//    }
//
//    @java.lang.Override
//    @java.lang.SuppressWarnings("unchecked")
//    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
//      switch (methodId) {
//        case METHODID_SAY_HELLO:
//          serviceImpl.sayHello((com.fantion.backend.payment.tossgrpc.TossHelloRequest) request,
//              (io.grpc.stub.StreamObserver<com.fantion.backend.payment.tossgrpc.TossHelloReply>) responseObserver);
//          break;
//        case METHODID_SAY_HELLO_AGAIN:
//          serviceImpl.sayHelloAgain((com.fantion.backend.payment.tossgrpc.TossHelloRequest) request,
//              (io.grpc.stub.StreamObserver<com.fantion.backend.payment.tossgrpc.TossHelloReply>) responseObserver);
//          break;
//        default:
//          throw new AssertionError();
//      }
//    }
//
//    @java.lang.Override
//    @java.lang.SuppressWarnings("unchecked")
//    public io.grpc.stub.StreamObserver<Req> invoke(
//        io.grpc.stub.StreamObserver<Resp> responseObserver) {
//      switch (methodId) {
//        default:
//          throw new AssertionError();
//      }
//    }
//  }
//
//  private static abstract class GreeterBaseDescriptorSupplier
//      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
//    GreeterBaseDescriptorSupplier() {}
//
//    @java.lang.Override
//    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
//      return com.fantion.backend.payment.tossgrpc.TossHelloWorldProto.getDescriptor();
//    }
//
//    @java.lang.Override
//    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
//      return getFileDescriptor().findServiceByName("Greeter");
//    }
//  }
//
//  private static final class GreeterFileDescriptorSupplier
//      extends GreeterBaseDescriptorSupplier {
//    GreeterFileDescriptorSupplier() {}
//  }
//
//  private static final class GreeterMethodDescriptorSupplier
//      extends GreeterBaseDescriptorSupplier
//      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
//    private final String methodName;
//
//    GreeterMethodDescriptorSupplier(String methodName) {
//      this.methodName = methodName;
//    }
//
//    @java.lang.Override
//    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
//      return getServiceDescriptor().findMethodByName(methodName);
//    }
//  }
//
//  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;
//
//  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
//    io.grpc.ServiceDescriptor result = serviceDescriptor;
//    if (result == null) {
//      synchronized (com.fantion.backend.payment.tossgrpc.TossGreeterGrpc.class) {
//        result = serviceDescriptor;
//        if (result == null) {
//          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
//              .setSchemaDescriptor(new GreeterFileDescriptorSupplier())
//              .addMethod(getSayHelloMethod())
//              .addMethod(getSayHelloAgainMethod())
//              .build();
//        }
//      }
//    }
//    return result;
//  }
//}
