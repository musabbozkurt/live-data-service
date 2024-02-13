package com.mb.livedataservice.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.playintegrity.v1.PlayIntegrity;
import com.google.api.services.playintegrity.v1.model.DecodeIntegrityTokenRequest;
import com.google.api.services.playintegrity.v1.model.DecodeIntegrityTokenResponse;
import com.mb.livedataservice.api.request.ApiPlayIntegrityTokenResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayIntegrityService {

    private static final String RESULT = "result";
    private static final String SUCCEEDED = "succeeded";

    private final PlayIntegrity playIntegrity;

    public Map<String, String> createNonce() {
        String createdNonce = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));

        HashMap<String, String> map = new HashMap<>();
        map.put("createdNonce", createdNonce);
        log.info("Nonce is created createNonce: {}", map);
        return map;
    }

    public Map<String, Object> verifyToken(ApiPlayIntegrityTokenResult tokenResult) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            DecodeIntegrityTokenResponse response = playIntegrity.v1()
                    .decodeIntegrityToken(tokenResult.getPackageName(), new DecodeIntegrityTokenRequest().setIntegrityToken(tokenResult.getToken()))
                    .execute();

            map.put(RESULT, response.getTokenPayloadExternal());
            map.put(SUCCEEDED, true);
        } catch (Exception e) {
            log.error("Exception occurred while verifying play integrity token. Exception: {}", ExceptionUtils.getStackTrace(e));
            if (e instanceof GoogleJsonResponseException googleJsonResponseException) {
                map.put(RESULT, googleJsonResponseException.getDetails());
            } else {
                map.put(RESULT, e.getMessage());
            }
            map.put(SUCCEEDED, false);
        }
        log.info("Play Integrity Token verification result: {}", map);
        return map;
    }
}
