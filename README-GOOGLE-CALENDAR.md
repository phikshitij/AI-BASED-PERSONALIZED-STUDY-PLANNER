# Google Calendar Integration Setup

This document provides instructions on how to set up the Google Calendar API integration for the AI Study Planner application.

## Prerequisites

1. A Google account
2. Access to the [Google Cloud Console](https://console.cloud.google.com/)

## Steps to Set Up Google Calendar API

1. **Create a Google Cloud Project**:
   - Go to the [Google Cloud Console](https://console.cloud.google.com/)
   - Click on "Select a project" at the top of the page
   - Click on "New Project"
   - Enter a name for your project and click "Create"

2. **Enable the Google Calendar API**:
   - In your new project, go to "APIs & Services" > "Library"
   - Search for "Google Calendar API"
   - Click on the Google Calendar API and click "Enable"

3. **Create OAuth Credentials**:
   - Go to "APIs & Services" > "Credentials"
   - Click "Create Credentials" and select "OAuth client ID"
   - Select "Web application" as the application type
   - Set a name for your OAuth client
   - Add the following authorized redirect URIs:
     - `http://localhost:8888/Callback`
   - Add the following authorized JavaScript origins:
     - `http://localhost:8082`
   - Click "Create"

4. **Download Credentials**:
   - After creating the OAuth client ID, you'll see a modal with your client ID and client secret
   - Click "Download JSON"
   - Save the downloaded file as `credentials.json`

5. **Add Credentials to the Application**:
   - Replace the contents of `src/main/resources/credentials.json` with the downloaded file

## Using the Google Calendar Integration

1. When you first run the application and try to add a study plan to Google Calendar, you'll be prompted to authorize the application to access your Google Calendar.
2. Follow the authorization flow in your browser.
3. Once authorized, the application will be able to add study plans to your Google Calendar.

## Troubleshooting

- If you encounter any issues with the Google Calendar integration, check the application logs for detailed error messages.
- Ensure that the `credentials.json` file contains valid credentials and not placeholder values.
- Make sure the redirect URIs and JavaScript origins in your Google Cloud Console match those in the application.
