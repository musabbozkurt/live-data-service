package com.mb.livedataservice.service.grpc;

import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import io.grpc.stub.StreamObserver;
import livedataservice.proto.HelloReply;
import livedataservice.proto.HelloRequest;
import livedataservice.proto.SimpleGrpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class GrpcServerService extends SimpleGrpc.SimpleImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        log.info("Request received for HelloRequest: {}", request);
        if (request.getName().startsWith("error")) {
            throw new IllegalArgumentException("Bad name: %s".formatted(request.getName()));
        }
        if (request.getName().startsWith("internal")) {
            throw new BaseException(LiveDataErrorCode.INVALID_VALUE);
        }
        String greeting = StringUtils.isBlank(request.getGreeting()) ? "Hello" : request.getGreeting();
        HelloReply reply = HelloReply.newBuilder().setMessage("%s ==> %s".formatted(greeting, request.getName())).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void streamHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        log.info("Streaming request received for HelloRequest: {}", request);
        String greeting = StringUtils.isBlank(request.getGreeting()) ? "Hello" : request.getGreeting();
        int count = 0;
        while (count < 10) {
            HelloReply reply = HelloReply.newBuilder().setMessage("%s(%d) ==> %s".formatted(greeting, count, request.getName())).build();
            responseObserver.onNext(reply);
            count++;
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                responseObserver.onError(e);
                return;
            }
        }
        responseObserver.onCompleted();
    }
}
