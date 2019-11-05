package org.guitartext;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableCaching
public class Application {

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);

    public static void main(final String[] args) throws IOException, GeneralSecurityException {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    NetHttpTransport netHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    JsonFactory jsonFactory() {
        return JacksonFactory.getDefaultInstance();
    }

    @Bean
    GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow(
            final NetHttpTransport netHttpTransport,
            final JsonFactory jsonFactory,
            @Value("${app.scopes}") final String[] scopes
    ) throws IOException {

        final var factory = new GoogleFlowFactory(netHttpTransport, jsonFactory);
        return factory.create(scopes);
    }

    @Configuration
    static class CaffeineCustomizer implements CacheManagerCustomizer<CaffeineCacheManager> {

        @Override
        public void customize(final CaffeineCacheManager cacheManager) {
            final Caffeine<Object, Object> builder = Caffeine.newBuilder()
                    .expireAfterWrite(10, TimeUnit.DAYS);

            cacheManager.setCaffeine(builder);
        }
    }
}
