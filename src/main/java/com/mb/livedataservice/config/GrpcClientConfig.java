package com.mb.livedataservice.config;

import livedataservice.proto.SimpleGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {

    @Value("${spring.grpc.client.hello-service.address:localhost:9091}")
    private String grpcServerAddress;

    @Bean
    public SimpleGrpc.SimpleBlockingStub simpleBlockingStub(GrpcChannelFactory channelFactory) {
        return SimpleGrpc.newBlockingStub(channelFactory.createChannel(grpcServerAddress));
    }

    @Bean
    public SimpleGrpc.SimpleStub simpleAsyncStub(GrpcChannelFactory channelFactory) {
        return SimpleGrpc.newStub(channelFactory.createChannel(grpcServerAddress));
    }
}
