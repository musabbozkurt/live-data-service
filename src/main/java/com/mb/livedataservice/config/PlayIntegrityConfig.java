package com.mb.livedataservice.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.playintegrity.v1.PlayIntegrity;
import com.google.api.services.playintegrity.v1.PlayIntegrityRequestInitializer;
import com.google.api.services.playintegrity.v1.PlayIntegrityScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

@Slf4j
@Setter
@Configuration
@ConfigurationProperties("clients.play-integrity-api")
public class PlayIntegrityConfig {

    private String credentialsFile;
    private String applicationName;

    @Bean
    public PlayIntegrity playIntegrity() throws GeneralSecurityException, IOException {
        try {
            ClassPathResource classPathResource = new ClassPathResource(credentialsFile);
            if (classPathResource.exists()) {
                return buildPlayIntegrityWithInputStream(classPathResource.getInputStream());
            }
        } catch (Exception e) {
            log.error("Exception occurred while building PlayIntegrity bean. Exception: {}", ExceptionUtils.getStackTrace(e));
        }
        return buildPlayIntegrityWithCredentials(GoogleCredentials.newBuilder().build());
    }

    private PlayIntegrity buildPlayIntegrityWithInputStream(InputStream targetStream) throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(targetStream)
                .createScoped(PlayIntegrityScopes.PLAYINTEGRITY);
        credentials.refreshIfExpired();

        return buildPlayIntegrityWithCredentials(credentials);
    }

    private PlayIntegrity buildPlayIntegrityWithCredentials(GoogleCredentials googleCredentials) throws GeneralSecurityException, IOException {
        return new PlayIntegrity.Builder(GoogleNetHttpTransport.newTrustedTransport(), new GsonFactory(), new HttpCredentialsAdapter(googleCredentials))
                .setApplicationName(applicationName)
                .setGoogleClientRequestInitializer(new PlayIntegrityRequestInitializer())
                .build();
    }
}
