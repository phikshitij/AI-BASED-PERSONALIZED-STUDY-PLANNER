package com.project.studyPlanner.controller;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.project.studyPlanner.service.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
public class GoogleCalendarController {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String CALLBACK_URL = "http://localhost:8082/Callback";
    
    @Autowired
    private GoogleCalendarService googleCalendarService;

    /**
     * Add a study plan to Google Calendar
     * 
     * @param requestData Map containing userId and subject
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addToCalendar(@RequestBody Map<String, String> requestData) {
        String userId = requestData.get("userId");
        String subject = requestData.get("subject");
        
        if (userId == null || userId.isEmpty() || subject == null || subject.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required parameters: userId and/or subject"));
        }
        
        try {
            // Use the actual Google Calendar service
            String result = googleCalendarService.addStudyPlanToCalendar(userId, subject);
            
            if (result.startsWith("Error") || result.startsWith("Please authorize")) {
                // If authentication is needed, redirect to auth page
                if (result.contains("authorize")) {
                    return ResponseEntity.status(401).body(Map.of(
                        "error", result,
                        "redirectUrl", "/api/calendar/auth"
                    ));
                }
                return ResponseEntity.status(500).body(Map.of("error", result));
            }
            
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Endpoint to initiate Google Calendar authorization
     * 
     * @return RedirectView to Google's authorization page
     */
    @GetMapping("/auth")
    public RedirectView initiateAuth() {
        try {
            GoogleAuthorizationCodeFlow flow = getFlow();
            AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl()
                    .setRedirectUri(CALLBACK_URL)
                    .setAccessType("offline");
            
            return new RedirectView(authorizationUrl.build());
        } catch (Exception e) {
            // If there's an error, redirect to an error page
            return new RedirectView("/auth.html?error=" + e.getMessage());
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
        InputStream in = GoogleCalendarController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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
