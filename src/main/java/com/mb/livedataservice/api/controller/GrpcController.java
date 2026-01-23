package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.api.request.ApiHelloRequest;
import com.mb.livedataservice.api.response.ApiHelloResponse;
import jakarta.validation.Valid;
import livedataservice.proto.HelloReply;
import livedataservice.proto.HelloRequest;
import livedataservice.proto.SimpleGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * REST Controller that exposes gRPC service endpoints via HTTP.
 * <p>
 * This controller acts as a bridge between HTTP clients and the gRPC server,
 * allowing clients to interact with gRPC services using standard HTTP requests.
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/grpc")
public class GrpcController {

    private final SimpleGrpc.SimpleBlockingStub simpleBlockingStub;

    /**
     * Virtual thread executor for handling blocking gRPC streaming calls.
     * <p>
     * Virtual threads (Project Loom) are lightweight and ideal for I/O-bound operations
     * like waiting for gRPC responses, without blocking platform threads.
     * </p>
     */
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Unary gRPC call endpoint - sends a single request and receives a single response.
     *
     * @param request the hello request containing name and optional greeting
     * @return the greeting response from the gRPC server
     */
    @PostMapping("/hello")
    public ApiHelloResponse sayHello(@Valid @RequestBody ApiHelloRequest request) {
        log.info("Received request to sayHello. ApiHelloRequest: {}", request);

        // Build the gRPC request from the API request
        HelloRequest grpcRequest = HelloRequest.newBuilder()
                .setName(request.name())
                .setGreeting(StringUtils.isNotBlank(request.greeting()) ? request.greeting() : "")
                .build();

        // Make a blocking unary gRPC call
        HelloReply reply = simpleBlockingStub.sayHello(grpcRequest);

        log.info("Received gRPC response: {}", reply);
        return new ApiHelloResponse(reply.getMessage());
    }

    /**
     * Server-Sent Events (SSE) streaming endpoint that bridges gRPC server streaming to HTTP.
     * <p>
     * This endpoint demonstrates how to convert a gRPC server streaming response into
     * an HTTP SSE stream, allowing real-time data to be pushed to HTTP clients.
     * </p>
     *
     * <h3>How it works:</h3>
     * <ol>
     *   <li>Client sends a POST request with name and greeting</li>
     *   <li>Server creates an SseEmitter and returns it immediately (non-blocking)</li>
     *   <li>A virtual thread is spawned to handle the blocking gRPC streaming call</li>
     *   <li>Each gRPC response is converted to an SSE event and sent to the client</li>
     *   <li>Events are flushed immediately, enabling real-time streaming</li>
     *   <li>Stream completes when all gRPC responses are received</li>
     * </ol>
     *
     * @param request the hello request containing name and optional greeting
     * @return SseEmitter that streams events to the client in real-time
     */
    @PostMapping(value = "/hello/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamHello(@Valid @RequestBody ApiHelloRequest request) {
        log.info("Received request to streamHello. ApiHelloRequest: {}", request);

        // Create SseEmitter with 60-second timeout
        // SseEmitter is Spring MVC's mechanism for Server-Sent Events
        // It allows pushing data to the client without the client polling
        SseEmitter emitter = new SseEmitter(60000L);

        // Build the gRPC request
        HelloRequest grpcRequest = HelloRequest.newBuilder()
                .setName(request.name())
                .setGreeting(StringUtils.isNotBlank(request.greeting()) ? request.greeting() : "")
                .build();

        // Capture the current MDC context which contains traceId and spanId
        // This is necessary because the virtual thread won't inherit the MDC context automatically
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        // Execute the blocking gRPC call in a separate virtual thread
        // This prevents blocking the servlet container's thread pool
        executor.execute(() -> {
            // Restore the MDC context in the new thread
            // This ensures traceId and spanId are available in logs
            if (mdcContext != null) {
                MDC.setContextMap(mdcContext);
            }

            try {
                // streamHello returns an Iterator that blocks on each next() call
                // waiting for the gRPC server to send the next message
                Iterator<HelloReply> replies = simpleBlockingStub.streamHello(grpcRequest);
                int count = 0;

                // Process each gRPC response as it arrives
                while (replies.hasNext()) {
                    HelloReply reply = replies.next();
                    log.info("Streaming gRPC response: {}", reply);

                    // Build an SSE event with:
                    // - id: unique identifier for the event (useful for client reconnection)
                    // - name/event: event type that client can listen for
                    // - data: the actual payload serialized as JSON
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .id(String.valueOf(count++))
                            .name("message")
                            .data(new ApiHelloResponse(reply.getMessage()), MediaType.APPLICATION_JSON);

                    // Send the event to the client
                    // This call flushes the response immediately, enabling real-time streaming
                    emitter.send(event);
                }

                // Log completion while we still have MDC context
                log.info("SSE stream completed successfully. Total events sent: {}", count);

                // Signal that the stream is complete
                emitter.complete();
            } catch (IOException e) {
                // IOException typically means the client disconnected
                log.error("Error sending SSE event (client may have disconnected). Exception: {}", ExceptionUtils.getStackTrace(e));
                emitter.completeWithError(e);
            } catch (Exception e) {
                // Handle any other errors (e.g., gRPC errors)
                log.error("Error during gRPC streaming. Exception: {}", ExceptionUtils.getStackTrace(e));
                emitter.completeWithError(e);
            } finally {
                // Clean up MDC to prevent memory leaks in thread pools
                MDC.clear();
            }
        });

        // Register lifecycle callbacks for monitoring and cleanup
        // Note: These callbacks run on different threads without MDC context
        // For traced logging, use the log statements inside the executor above
        emitter.onCompletion(() -> {
            if (mdcContext != null) {
                MDC.setContextMap(mdcContext);
            }
            try {
                log.debug("SseEmitter onCompletion callback triggered");
            } finally {
                MDC.clear();
            }
        });
        emitter.onTimeout(() -> {
            if (mdcContext != null) {
                MDC.setContextMap(mdcContext);
            }
            try {
                log.warn("SSE stream timed out");
            } finally {
                MDC.clear();
            }
        });
        emitter.onError(e -> {
            if (mdcContext != null) {
                MDC.setContextMap(mdcContext);
            }
            try {
                log.error("SSE stream error. Exception: {}", ExceptionUtils.getStackTrace(e));
            } finally {
                MDC.clear();
            }
        });

        // Return the emitter immediately - the actual streaming happens asynchronously
        return emitter;
    }
}
