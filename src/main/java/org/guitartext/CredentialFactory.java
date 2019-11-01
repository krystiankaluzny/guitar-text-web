package org.guitartext;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

class CredentialFactory {

    private static final String CREDENTIALS_FILE_PATH = "/client_id.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private final NetHttpTransport netHttpTransport;
    private final JsonFactory jsonFactory;

    CredentialFactory(final NetHttpTransport netHttpTransport, final JsonFactory jsonFactory) {
        this.netHttpTransport = netHttpTransport;
        this.jsonFactory = jsonFactory;
    }

    Credential create(final String host, final int port, final String callbackPath, final String scope) throws IOException {
        final GoogleClientSecrets clientSecrets = loadGoogleClientSecrets();

        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                netHttpTransport, jsonFactory, clientSecrets, Collections.singleton(scope))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        final LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setHost(host)
                .setPort(port)
                .setCallbackPath(callbackPath)
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver)
                .authorize("user");
    }

    private GoogleClientSecrets loadGoogleClientSecrets() throws IOException {

        final InputStream in = CredentialFactory.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        return GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));
    }
}
