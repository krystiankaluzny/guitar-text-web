package org.guitartext;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class Application {

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);

    public static void main(final String[] args) throws IOException, GeneralSecurityException {
        SpringApplication.run(Application.class, args);
//
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        final Credential httpRequestInitializer = CredentialFactory.create(HTTP_TRANSPORT, SCOPES);
//        Drive service = new Drive.Builder(HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(), httpRequestInitializer)
//                .setApplicationName("guitartext")
//                .build();
//
//        // Print the names and IDs for up to 10 files.
//        FileList result = service.files().list()
//                .setPageSize(10)
//                .setFields("nextPageToken, files(id, name)")
//                .execute();
//        List<File> files = result.getFiles();
//        if (files == null || files.isEmpty()) {
//            System.out.println("No files found.");
//        } else {
//            System.out.println("Files:");
//            for (File file : files) {
//                System.out.printf("%s (%s)\n", file.getName(), file.getId());
//            }
//        }
    }

    @Bean
    NetHttpTransport netHttpTransport() throws GeneralSecurityException, IOException {
        System.out.println("DUPA");
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    JsonFactory jsonFactory() {
        System.out.println("DUPA2");
        return JacksonFactory.getDefaultInstance();
    }

    @Bean
    Credential credential(
            final NetHttpTransport netHttpTransport,
            final JsonFactory jsonFactory,
            @Value("${app.receiver.host}") final String host,
            @Value("${app.receiver.port}") final int port,
            @Value("${app.receiver.callback.path}") final String callbackPath,
            @Value("${app.scope}") final String scope
    ) throws IOException {
        System.out.println(scope);
        final var factory = new CredentialFactory(netHttpTransport, jsonFactory);
        return factory.create(host, port, callbackPath, scope);
    }
}
