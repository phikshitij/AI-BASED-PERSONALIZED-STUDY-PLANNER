package com.project.studyPlanner.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.project.studyPlanner.models.StudyPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "AI Study Planner";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    @Autowired
    private DBServices dbServices;

    /**
     * Creates an authorized Credential object.
     * 
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        
        GoogleClientSecrets clientSecrets;
        try {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            
            // Check if the client ID and client secret are placeholders
            String clientId = clientSecrets.getDetails().getClientId();
            String clientSecret = clientSecrets.getDetails().getClientSecret();
            
            if ("YOUR_CLIENT_ID".equals(clientId) || "YOUR_CLIENT_SECRET".equals(clientSecret)) {
                throw new IOException("Please update the credentials.json file with your actual Google API credentials");
            }
            
        } catch (Exception e) {
            throw new IOException("Error loading client secrets: " + e.getMessage(), e);
        }

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        
        // For web application, we'll use a different approach
        // We'll use a pre-authorized user ID for now
        return flow.loadCredential("user");
    }

    /**
     * Build and return an authorized Calendar client service.
     * 
     * @return An authorized Calendar client service
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private Calendar getCalendarService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Clear existing tokens to force re-authentication
     */
    private void clearTokens() {
        try {
            File tokensFolder = new File(TOKENS_DIRECTORY_PATH);
            if (tokensFolder.exists() && tokensFolder.isDirectory()) {
                File[] files = tokensFolder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            boolean deleted = file.delete();
                            if (!deleted) {
                                System.err.println("Failed to delete token file: " + file.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error clearing tokens: " + e.getMessage());
        }
    }

    /**
     * Add study plan to Google Calendar
     * 
     * @param userId The user ID
     * @param subjectName The subject name
     * @return A message indicating success or failure
     */
    public String addStudyPlanToCalendar(String userId, String subjectName) {
        try {
            // Get the study plan from the database
            List<StudyPlan> studyPlans = dbServices.getStudyPlansBySubject(subjectName);
            if (studyPlans.isEmpty()) {
                return "Study plan not found for subject: " + subjectName;
            }
            StudyPlan studyPlan = studyPlans.get(0);
            String studyData = studyPlan.getStudyData();
            
            if (studyData == null || studyData.isEmpty()) {
                return "Study plan data is empty for subject: " + subjectName;
            }
            
            // Create a list of calendar events from the study data
            List<Map<String, String>> calendarEvents = new ArrayList<>();
            
            // Check if the data is in HTML format (contains HTML tags)
            if (studyData.contains("<h3>") || studyData.contains("<p>") || studyData.contains("<div>")) {
                // Parse HTML content to extract days and topics
                String[] days = studyData.split("<h3>");
                
                for (int i = 1; i < days.length; i++) { // Start from 1 to skip any content before the first <h3>
                    String day = days[i];
                    
                    // Extract day title and hours
                    String dayTitle = day.substring(0, day.indexOf("</h3>")).trim();
                    
                    // Extract hours from the day title (e.g., "Day 1: Introduction (5 hours)")
                    int hoursIndex = dayTitle.lastIndexOf("(");
                    int hours = 1; // Default to 1 hour
                    
                    if (hoursIndex != -1) {
                        String hoursStr = dayTitle.substring(hoursIndex + 1, dayTitle.lastIndexOf(")")).replace("hours", "").replace("hour", "").trim();
                        try {
                            hours = Integer.parseInt(hoursStr);
                        } catch (NumberFormatException e) {
                            // Keep default 1 hour
                        }
                    }
                    
                    Map<String, String> event = new HashMap<>();
                    event.put("topic", dayTitle);
                    event.put("hours", String.valueOf(hours));
                    calendarEvents.add(event);
                }
            } else {
                // Try to parse as JSON array
                try {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> parsedData = gson.fromJson(studyData, List.class);
                    
                    if (parsedData != null && !parsedData.isEmpty()) {
                        for (Map<String, Object> day : parsedData) {
                            Map<String, String> event = new HashMap<>();
                            event.put("topic", day.getOrDefault("topic", "Study Session").toString());
                            event.put("hours", day.getOrDefault("hours", "1").toString());
                            calendarEvents.add(event);
                        }
                    } else {
                        // Create a default event if parsing fails
                        Map<String, String> event = new HashMap<>();
                        event.put("topic", subjectName + " Study Session");
                        event.put("hours", "1");
                        calendarEvents.add(event);
                    }
                } catch (Exception e) {
                    // If JSON parsing fails, create a default event
                    Map<String, String> event = new HashMap<>();
                    event.put("topic", subjectName + " Study Session");
                    event.put("hours", "1");
                    calendarEvents.add(event);
                }
            }
            
            // If no events were created, return an error
            if (calendarEvents.isEmpty()) {
                return "Could not create any calendar events from the study data";
            }
            
            try {
                // Check if we have valid credentials
                File tokensDir = new File(TOKENS_DIRECTORY_PATH);
                if (!tokensDir.exists() || tokensDir.listFiles() == null || tokensDir.listFiles().length == 0) {
                    return "Please authorize the application with Google Calendar first. Visit: http://localhost:8082/api/calendar/auth";
                }
                
                // Get the calendar service
                Calendar service = getCalendarService();
                
                // Start date for the study plan (today if not specified)
                LocalDate startDate = LocalDate.now();
                
                // Add events to the calendar
                for (int i = 0; i < calendarEvents.size(); i++) {
                    Map<String, String> eventData = calendarEvents.get(i);
                    
                    // Create event
                    Event event = new Event()
                            .setSummary("Study: " + subjectName)
                            .setDescription("Topic: " + eventData.get("topic"));
                    
                    // Set start and end time
                    LocalDateTime eventStart = startDate.plusDays(i).atTime(9, 0); // Default to 9 AM
                    
                    // Parse hours, defaulting to 1 if there's an issue
                    int hours = 1;
                    try {
                        hours = Integer.parseInt(eventData.get("hours"));
                    } catch (NumberFormatException e) {
                        // Use default of 1 hour
                    }
                    
                    LocalDateTime eventEnd = eventStart.plusHours(hours);
                    
                    DateTime startDateTime = new DateTime(Date.from(eventStart.atZone(ZoneId.systemDefault()).toInstant()));
                    DateTime endDateTime = new DateTime(Date.from(eventEnd.atZone(ZoneId.systemDefault()).toInstant()));
                    
                    EventDateTime start = new EventDateTime()
                            .setDateTime(startDateTime)
                            .setTimeZone("Asia/Kolkata"); // Use appropriate timezone
                    EventDateTime end = new EventDateTime()
                            .setDateTime(endDateTime)
                            .setTimeZone("Asia/Kolkata");
                    
                    event.setStart(start);
                    event.setEnd(end);
                    
                    // Add reminders
                    EventReminder[] reminderOverrides = new EventReminder[] {
                            new EventReminder().setMethod("email").setMinutes(24 * 60), // 1 day before
                            new EventReminder().setMethod("popup").setMinutes(30) // 30 minutes before
                    };
                    
                    Event.Reminders reminders = new Event.Reminders()
                            .setUseDefault(false)
                            .setOverrides(Arrays.asList(reminderOverrides));
                    event.setReminders(reminders);
                    
                    // Insert the event
                    event = service.events().insert("primary", event).execute();
                }
                
                return "Study plan successfully added to Google Calendar!";
            } catch (FileNotFoundException e) {
                return "Google Calendar credentials file not found. Please set up your credentials.";
            } catch (IOException e) {
                // If it's an authentication issue, clear tokens and suggest re-authorization
                if (e.getMessage().contains("401") || e.getMessage().contains("auth") || e.getMessage().contains("Authentication")) {
                    clearTokens();
                    return "Authentication error. Please authorize the application again: http://localhost:8082/api/calendar/auth";
                }
                return "Error with Google Calendar: " + e.getMessage();
            } catch (GeneralSecurityException e) {
                return "Security error with Google Calendar: " + e.getMessage();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Error adding study plan to calendar: " + e.getMessage();
        }
    }
}
