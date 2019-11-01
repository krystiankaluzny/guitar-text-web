package org.guitartext;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

@Controller
class AppController {

    private final String userId = "user";
    private final GoogleAuthorizationCodeFlow flow;
    private String redirectUri;

    public AppController(final GoogleAuthorizationCodeFlow flow,
                         @Value("${app.redirect.uri}") final String redirectUri) {
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
}
