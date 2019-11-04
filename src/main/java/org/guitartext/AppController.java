package org.guitartext;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
class AppController {

    private final String userId = "user";

    private final NetHttpTransport netHttpTransport;
    private final JsonFactory jsonFactory;

    private final GoogleAuthorizationCodeFlow flow;
    private String redirectUri;

    public AppController(final NetHttpTransport netHttpTransport, final JsonFactory jsonFactory, final GoogleAuthorizationCodeFlow flow,
                         @Value("${app.redirect.uri}") final String redirectUri) {
        this.netHttpTransport = netHttpTransport;
        this.jsonFactory = jsonFactory;
        this.flow = flow;
        this.redirectUri = redirectUri;
    }

    @GetMapping("/")
    String home() throws IOException {
        final Credential credential = flow.loadCredential(userId);

        if (credential != null && credential.refreshToken()) {
            return "index.html";
        }

        return "signin.html";
    }

    @GetMapping("/signin")
    View signIn() {
        final GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        final var googleRedirect = url.setRedirectUri(redirectUri).setAccessType("offline").build();
        return new RedirectView(googleRedirect);
    }

    @GetMapping("/oauth2callback")
    String oauth2Callback(@RequestParam("code") final String code) throws IOException {

        if (code != null) {
            final GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
            flow.createAndStoreCredential(response, userId);

            return "index.html";
        }

        return "signin.html";
    }

    @GetMapping(value = "/list/files", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    String listFiles() throws IOException {

        final Credential credential = flow.loadCredential(userId);
        final Drive service = new Drive.Builder(netHttpTransport, jsonFactory, credential)
                .setApplicationName("guitartext")
                .build();
//
        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setQ("\"root\" in parents")
                .setQ("\"0Bw5tkuaQCdftbnMtYkNVc2ZaQWc\" in parents")
                .setFields("files(id, name, parents, mimeType)")
                .execute();

        Map<String, String> r = new HashMap<>();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
                final String value = String.format("{id: %s, name: %s, mimeType: %s, parents: %s}",
                        file.getId(),
                        file.getName(),
                        file.getMimeType(),
                        String.valueOf(file.getParents())
                );
                r.put(file.getId(), value);
            }
        }

        System.out.println("DONE");
        return jsonFactory.toPrettyString(r);
    }
}
