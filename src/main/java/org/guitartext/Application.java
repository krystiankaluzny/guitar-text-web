package org.guitartext;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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

//    @Bean
//    Drive drive(
//            final NetHttpTransport netHttpTransport,
//            final JsonFactory jsonFactory,
//            final Credential credential
//    ) {
//        return new Drive.Builder(netHttpTransport, jsonFactory, credential)
//                .setApplicationName("GuitarText")
//                .build();
//    }
}
