package com.mb.livedataservice.integration_tests.grpc;

import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import livedataservice.proto.HelloReply;
import livedataservice.proto.HelloRequest;
import livedataservice.proto.SimpleGrpc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.LocalGrpcPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.GrpcChannelFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestcontainersConfiguration.class,
        properties = {
                "spring.grpc.server.port=0"
        }
)
class GrpcServerServiceTest {

    @LocalGrpcPort
    private int grpcPort;

    @Autowired
    private GrpcChannelFactory channelFactory;

    private static Stream<Arguments> provideNamesForSayHello() {
        return Stream.of(
                Arguments.of("World", "Hello ==> World"),
                Arguments.of("", "Hello ==> "),
                Arguments.of("Test!@#$%^&*()", "Hello ==> Test!@#$%^&*()"),
                Arguments.of("世界", "Hello ==> 世界")
        );
    }

    @MethodSource("provideNamesForSayHello")
    @ParameterizedTest(name = "sayHello with name=''{0}'' should return ''{1}''")
    void sayHello_ShouldReturnGreeting_WhenNameProvided(String name, String expectedMessage) {
        // Arrange
        SimpleGrpc.SimpleBlockingStub stub = createBlockingStub();
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();

        // Act
        HelloReply reply = stub.sayHello(request);

        // Assertions
        assertNotNull(reply);
        assertEquals(expectedMessage, reply.getMessage());
    }

    @Test
    void streamHello_ShouldReturnAllRepliesCorrectly() {
        // Arrange
        SimpleGrpc.SimpleBlockingStub stub = createBlockingStub();
        HelloRequest request = HelloRequest.newBuilder()
                .setName("StreamTest")
                .setGreeting("")
                .build();

        // Act
        Iterator<HelloReply> replies = stub.streamHello(request);
        List<HelloReply> replyList = new ArrayList<>();
        replies.forEachRemaining(replyList::add);

        // Assertions
        assertEquals(10, replyList.size());
        assertAll("All stream replies should match expected messages",
                IntStream.range(0, 10)
                        .mapToObj(i -> () -> assertEquals("Hello(%d) ==> StreamTest".formatted(i), replyList.get(i).getMessage(), "Reply at index %d should match".formatted(i)))
        );
    }

    @Test
    void streamHello_ShouldReturnAllRepliesWithCustomGreeting() {
        // Arrange
        SimpleGrpc.SimpleBlockingStub stub = createBlockingStub();
        HelloRequest request = HelloRequest.newBuilder()
                .setName("StreamTest")
                .setGreeting("Hola")
                .build();

        // Act
        Iterator<HelloReply> replies = stub.streamHello(request);
        List<HelloReply> replyList = new ArrayList<>();
        replies.forEachRemaining(replyList::add);

        // Assertions
        assertEquals(10, replyList.size());
        assertAll("All stream replies should use custom greeting",
                IntStream.range(0, 10)
                        .mapToObj(i -> () -> assertEquals("Hola(%d) ==> StreamTest".formatted(i), replyList.get(i).getMessage(), "Reply at index %d should match".formatted(i)))
        );
    }

    @Test
    void streamHello_ShouldReturnRepliesInOrder() {
        // Arrange
        SimpleGrpc.SimpleBlockingStub stub = createBlockingStub();
        HelloRequest request = HelloRequest.newBuilder()
                .setName("OrderTest")
                .setGreeting("")
                .build();

        // Act
        Iterator<HelloReply> replies = stub.streamHello(request);
        List<HelloReply> replyList = new ArrayList<>();
        replies.forEachRemaining(replyList::add);

        // Assertions
        assertEquals(10, replyList.size());
        assertAll("All stream replies should be in order",
                IntStream.range(0, 10)
                        .mapToObj(i -> () -> assertTrue(replyList.get(i).getMessage().contains("(%d)".formatted(i)), "Reply at index %d should contain correct index".formatted(i)))
        );
    }

    @Test
    void streamHello_ShouldWorkWithAsyncStub() throws InterruptedException {
        // Arrange
        SimpleGrpc.SimpleStub asyncStub = createAsyncStub();
        HelloRequest request = HelloRequest.newBuilder()
                .setName("AsyncTest")
                .setGreeting("")
                .build();

        List<HelloReply> receivedReplies = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        // Act
        asyncStub.streamHello(request, new StreamObserver<>() {
            @Override
            public void onNext(HelloReply reply) {
                receivedReplies.add(reply);
            }

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        boolean completed = latch.await(30, TimeUnit.SECONDS);

        // Assertions
        assertTrue(completed, "Stream should complete within timeout");
        assertNull(errorRef.get(), "No error should occur");
        assertEquals(10, receivedReplies.size());
        assertAll("All async stream replies should match expected messages",
                IntStream.range(0, 10)
                        .mapToObj(i -> () -> assertEquals("Hello(%d) ==> AsyncTest".formatted(i), receivedReplies.get(i).getMessage(), "Async reply at index %d should match".formatted(i)))
        );
    }

    @Test
    void streamHello_ShouldReturnEmptyNameInReplies() {
        // Arrange
        SimpleGrpc.SimpleBlockingStub stub = createBlockingStub();
        HelloRequest request = HelloRequest.newBuilder()
                .setName("")
                .setGreeting("")
                .build();

        // Act
        Iterator<HelloReply> replies = stub.streamHello(request);
        List<HelloReply> replyList = new ArrayList<>();
        replies.forEachRemaining(replyList::add);

        // Assertions
        assertEquals(10, replyList.size());
        assertAll("All stream replies with empty name should match",
                IntStream.range(0, 10)
                        .mapToObj(i -> () -> assertEquals("Hello(%d) ==> ".formatted(i), replyList.get(i).getMessage(), "Reply at index %d should match".formatted(i)))
        );
    }

    @Test
    void sayHello_ShouldThrowException_WhenNameStartsWithError() {
        // Arrange
        SimpleGrpc.SimpleBlockingStub stub = createBlockingStub();
        HelloRequest request = HelloRequest.newBuilder()
                .setName("error-test")
                .setGreeting("")
                .build();

        // Act
        // Assertions
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> stub.sayHello(request));

        assertEquals(Status.UNKNOWN.getCode(), exception.getStatus().getCode());
    }

    @Test
    void sayHello_ShouldThrowException_WhenNameStartsWithInternal() {
        // Arrange
        SimpleGrpc.SimpleBlockingStub stub = createBlockingStub();
        HelloRequest request = HelloRequest.newBuilder()
                .setName("internal-test")
                .setGreeting("")
                .build();

        // Act
        // Assertions
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> stub.sayHello(request));

        assertEquals(Status.UNKNOWN.getCode(), exception.getStatus().getCode());
    }

    @Test
    void sayHello_ShouldHandleMultipleConcurrentRequests() throws InterruptedException {
        // Arrange
        int numberOfRequests = 5;
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        List<HelloReply> replies = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        // Act
        for (int i = 0; i < numberOfRequests; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    SimpleGrpc.SimpleBlockingStub stub = createBlockingStub();
                    HelloRequest request = HelloRequest.newBuilder()
                            .setName("Concurrent-" + index)
                            .setGreeting("")
                            .build();
                    HelloReply reply = stub.sayHello(request);
                    synchronized (replies) {
                        replies.add(reply);
                    }
                } catch (Throwable t) {
                    synchronized (errors) {
                        errors.add(t);
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);

        // Assertions
        assertTrue(completed, "All requests should complete within timeout");
        assertTrue(errors.isEmpty(), "No errors should occur: " + errors);
        assertEquals(numberOfRequests, replies.size());
    }

    @Test
    void sayHello_ShouldReturnGreeting_WhenLongNameProvided() {
        // Arrange
        SimpleGrpc.SimpleBlockingStub stub = createBlockingStub();
        String longName = "A".repeat(1000);
        HelloRequest request = HelloRequest.newBuilder()
                .setName(longName)
                .setGreeting("")
                .build();

        // Act
        HelloReply reply = stub.sayHello(request);

        // Assertions
        assertNotNull(reply);
        assertEquals("Hello ==> " + longName, reply.getMessage());
    }

    private SimpleGrpc.SimpleBlockingStub createBlockingStub() {
        return SimpleGrpc.newBlockingStub(channelFactory.createChannel("localhost:" + grpcPort));
    }

    private SimpleGrpc.SimpleStub createAsyncStub() {
        return SimpleGrpc.newStub(channelFactory.createChannel("localhost:" + grpcPort));
    }
}
