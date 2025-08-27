package com.project.studyPlanner.controller;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Controller
public class CallbackController {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String CALLBACK_URL = "http://localhost:8082/Callback";

    /**
     * Callback endpoint for Google OAuth
     * 
     * @param code Authorization code
     * @param error Error message
     * @return ResponseEntity with success or error message
     */
    @GetMapping("/Callback")
    @ResponseBody
    public ResponseEntity<String> handleCallback(@RequestParam(required = false) String code,
                                               @RequestParam(required = false) String error) {
        if (error != null) {
            return ResponseEntity.badRequest().body("Authorization failed: " + error);
        }
        
        if (code == null) {
            return ResponseEntity.badRequest().body("No authorization code provided");
        }
        
        try {
            GoogleAuthorizationCodeFlow flow = getFlow();
            TokenResponse response = flow.newTokenRequest(code)
                    .setRedirectUri(CALLBACK_URL)
                    .execute();
            
            flow.createAndStoreCredential(response, "user");
            
            String successHtml = "<html><head><title>Authorization Successful</title></head>" +
                    "<body style='font-family: Arial, sans-serif; text-align: center; margin-top: 50px;'>" +
                    "<h1>Authorization Successful!</h1>" +
                    "<p>You can now close this window and return to the application.</p>" +
                    "<script>setTimeout(function() { window.close(); }, 3000);</script>" +
                    "</body></html>";
            
            return ResponseEntity.ok()
                    .header("Content-Type", "text/html")
                    .body(successHtml);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing authorization: " + e.getMessage());
        }
    }
    
    /**
     * Create and configure the Google Authorization Code Flow
     * 
     * @return Configured GoogleAuthorizationCodeFlow
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private GoogleAuthorizationCodeFlow getFlow() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        // Load client secrets
        InputStream in = CallbackController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        
        // Build flow
        return new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, 
                Collections.singletonList(CalendarScopes.CALENDAR))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }
}
