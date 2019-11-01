package org.guitartext;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
class AppController {

    private final GoogleAuthorizationCodeFlow flow;
    private final String userId = "user";

    public AppController(final GoogleAuthorizationCodeFlow flow) {
        this.flow = flow;
    }

    @GetMapping("/")
    String home() throws IOException {
        final Credential credential = flow.loadCredential(userId);

        if(credential != null && credential.refreshToken()) {
            return "index.html";
        }

        return "test.html";
    }
}
