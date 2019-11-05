package org.guitartext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.List;

@Controller
class AppController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppController.class);

    private static final ObjectMapper mapper = new ObjectMapper();
    private final String userId = "user";

    private final FileService fileService;
    private final NetHttpTransport netHttpTransport;
    private final JsonFactory jsonFactory;
    private final GoogleAuthorizationCodeFlow flow;
    private String redirectUri;

    public AppController(final FileService fileService,
                         final NetHttpTransport netHttpTransport,
                         final JsonFactory jsonFactory,
                         final GoogleAuthorizationCodeFlow flow,
                         @Value("${app.redirect.uri}") final String redirectUri) {
        this.fileService = fileService;
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

    @GetMapping(value = "/file/{fileId}/children", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    String getChildren(@PathVariable("fileId") String fileId) throws IOException {

        LOGGER.info("START");
        final List<FileDTO> children = getChildrenFiles(fileId);

        LOGGER.info("STOP");
        return mapper.writeValueAsString(children);
    }

    @GetMapping(value = "/file/{fileId}/children/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    String refreshAndGetChildren(@PathVariable("fileId") String fileId) throws IOException {

        LOGGER.info("START");
        fileService.clearInfo(fileId);
        final List<FileDTO> children = getChildrenFiles(fileId);

        LOGGER.info("STOP");
        return mapper.writeValueAsString(children);
    }

    private List<FileDTO> getChildrenFiles(@PathVariable("fileId") String fileId) throws IOException {
        final Credential credential = flow.loadCredential(userId);
        final Drive service = new Drive.Builder(netHttpTransport, jsonFactory, credential)
                .setApplicationName("guitartext")
                .build();

        return fileService.getChildren(service, fileId);
    }
}
