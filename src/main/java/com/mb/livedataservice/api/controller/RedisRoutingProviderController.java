package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.service.RedisRoutingProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/redis-routings")
@Tag(name = "Redis Routing", description = "APIs for routing cache writes to a specific Redis cluster")
public class RedisRoutingProviderController {

    private final RedisRoutingProviderService redisRoutingProviderService;

    @PostMapping("/cache")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Value cached successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = """
                                    {"message":"cached","cluster":"x","key":"user:100"}
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Caching failed", content = @Content)
    })
    @Operation(
            summary = "Cache a value in a target Redis cluster",
            description = "Routes and stores the given key/value into one of configured clusters (e.g. x, y, z)."
    )
    public ResponseEntity<Map<String, Object>> cache(@RequestParam @NotBlank String cluster, @RequestParam @NotBlank String key, @RequestBody Object value) {
        redisRoutingProviderService.cacheData(cluster, key, value);
        return ResponseEntity.ok(Map.of("message", "cached", "cluster", cluster, "key", key));
    }

    @GetMapping("/cache")
    public ResponseEntity<Map<String, Object>> getAllKeys(@RequestParam @NotBlank String cluster) {
        var keys = redisRoutingProviderService.getAllKeys(cluster);
        return ResponseEntity.ok(Map.of("cluster", cluster, "keys", keys));
    }
}
