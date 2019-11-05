package org.guitartext;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

class GoogleFlowFactory {

    private static final String CREDENTIALS_FILE_PATH = "/client_id.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private final NetHttpTransport netHttpTransport;
    private final JsonFactory jsonFactory;

    GoogleFlowFactory(final NetHttpTransport netHttpTransport, final JsonFactory jsonFactory) {
        this.netHttpTransport = netHttpTransport;
        this.jsonFactory = jsonFactory;
    }

    GoogleAuthorizationCodeFlow create(final String[] scopes) throws IOException {
        final GoogleClientSecrets clientSecrets = loadGoogleClientSecrets();

        return new GoogleAuthorizationCodeFlow.Builder(
                netHttpTransport, jsonFactory, clientSecrets, Arrays.asList(scopes))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }

    private GoogleClientSecrets loadGoogleClientSecrets() throws IOException {

        final InputStream in = GoogleFlowFactory.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        return GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));
    }
}
