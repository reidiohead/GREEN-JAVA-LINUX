package org.example;

import javax.swing.JTextPane;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SonarQubeApiClient {

    public static void fetchAndPrintSonarQubeSmells(JTextPane consolePane, String sqUrl, String sqToken, String projectKey) {
        try {
            // SonarQube API Endpoint for Code Smells
            String endpoint = sqUrl + "/api/issues/search?componentKeys=" + projectKey + "&types=CODE_SMELL&resolved=false";

            // Basic Auth uses the token as the username, and a blank password
            String auth = sqToken + ":";
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            java.net.URL url = new java.net.URL(endpoint);
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Basic " + encodedAuth);

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String json = response.toString();

                // Lightweight extraction to avoid massive JSON dependencies
                int totalIndex = json.indexOf("\"total\":");
                if (totalIndex != -1) {
                    int commaIndex = json.indexOf(",", totalIndex);
                    String totalSmells = json.substring(totalIndex + 8, commaIndex);

                    Main.logToConsole(consolePane, "\n==================================================\n");
                    Main.logToConsole(consolePane, " SONARQUBE API RESULTS: " + totalSmells + " CODE SMELLS FOUND [YELLOW]\n");
                    Main.logToConsole(consolePane, "==================================================\n");

                    // Extract the first 5 issues to avoid flooding the terminal
                    String[] issues = json.split("\"component\":");
                    int displayLimit = Math.min(issues.length - 1, 5);

                    for (int i = 1; i <= displayLimit; i++) {
                        String issueStr = issues[i];

                        // Extract File Name
                        String fileStr = "Unknown File";
                        int fileStart = issueStr.indexOf("\"") + 1;
                        int fileEnd = issueStr.indexOf("\"", fileStart);
                        if(fileStart > 0 && fileEnd > fileStart) {
                            String fullPath = issueStr.substring(fileStart, fileEnd);
                            fileStr = fullPath.substring(fullPath.lastIndexOf(":") + 1); // Get just the filename
                        }

                        // Extract Message
                        String msgStr = "No message";
                        int msgStart = issueStr.indexOf("\"message\":\"");
                        if (msgStart != -1) {
                            msgStart += 11;
                            int msgEnd = issueStr.indexOf("\"", msgStart);
                            msgStr = issueStr.substring(msgStart, msgEnd);
                        }

                        // Extract Line Number
                        String lineStr = "N/A";
                        int lineStart = issueStr.indexOf("\"line\":");
                        if(lineStart != -1) {
                            lineStart += 7;
                            int lineEnd = issueStr.indexOf(",", lineStart);
                            lineStr = issueStr.substring(lineStart, lineEnd);
                        }

                        Main.logToConsole(consolePane, String.format(" -> [Line %s] %s: %s\n", lineStr, fileStr, msgStr));
                    }
                    if (issues.length - 1 > 5) {
                        Main.logToConsole(consolePane, " -> ... and " + ((issues.length - 1) - 5) + " more. View full dashboard for details.\n");
                    }
                    Main.logToConsole(consolePane, "--------------------------------------------------\n\n");
                }
            } else {
                Main.logToConsole(consolePane, "[WARNING] Could not fetch SonarQube API. HTTP Code: " + responseCode + "\n");
            }
        } catch (Exception e) {
            Main.logToConsole(consolePane, "[WARNING] SonarQube API Fetch Failed: " + e.getMessage() + "\n");
        }
    }
}