package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowerMonitor {
    private Process joularProcess;
    private double totalWatts = 0;
    private int readingCount = 0;
    private boolean isMonitoring = false;

    // Starts the hidden background process
    public void startMonitor(String exePath) {
        totalWatts = 0;
        readingCount = 0;
        isMonitoring = true;

        try {
            ProcessBuilder pb = new ProcessBuilder(exePath);
            pb.redirectErrorStream(true);
            joularProcess = pb.start();

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(joularProcess.getInputStream()))) {
                    String line;
                    // Regex to find the number between "CPU" and "W"
                    Pattern powerPattern = Pattern.compile("CPU\\s+([0-9.]+)\\s+W");

                    while (isMonitoring && (line = reader.readLine()) != null) {
                        // Strip hidden ANSI color codes from the CLI output
                        String cleanLine = line.replaceAll("\u001B\\[[;\\d]*m", "");

                        Matcher matcher = powerPattern.matcher(cleanLine);
                        if (matcher.find()) {
                            double currentWatts = Double.parseDouble(matcher.group(1));
                            totalWatts += currentWatts;
                            readingCount++;
                            // Optional: Print to console so you can see it working
                            System.out.println("[Joular CLI] Read: " + currentWatts + " W");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error reading Joular CLI: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Failed to start JoularCore: " + e.getMessage());
        }
    }

    // Kills the process and returns the average
    public double stopAndGetAverage() {
        isMonitoring = false;
        if (joularProcess != null) {
            joularProcess.destroy();
        }
        if (readingCount == 0) return 0.0;
        return totalWatts / readingCount;
    }
}