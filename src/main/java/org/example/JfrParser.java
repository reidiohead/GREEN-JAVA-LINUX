package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JfrParser {

    public static double extractMemoryAllocation(String folderPath) {
        double totalAllocatedBytes = 0.0;

        // 1. Find ALL profile.jfr files in the project (works for any benchmark name)
        List<File> allJfrFiles = new ArrayList<>();
        findAllJfrFiles(new File(folderPath), allJfrFiles);

        if (allJfrFiles.isEmpty()) {
            System.err.println("JFR Parser: No profile.jfr files found in: " + folderPath);
            return 0.0;
        }

        // 2. Loop through every benchmark JFR file found
        for (File jfrFile : allJfrFiles) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "jfr", "print", "--events", "jdk.ThreadAllocationStatistics", jfrFile.getAbsolutePath()
                );

                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                Map<String, Long> threadMap = new HashMap<>();
                Pattern threadPattern = Pattern.compile("thread = \"(.*?)\"");
                Pattern allocatedPattern = Pattern.compile("allocated = ([\\d\\.]+)\\s*(bytes|kB|MB|GB)?", Pattern.CASE_INSENSITIVE);

                String line;
                String currentThread = null;

                while ((line = reader.readLine()) != null) {
                    Matcher tMatcher = threadPattern.matcher(line);
                    if (tMatcher.find()) currentThread = tMatcher.group(1);

                    Matcher aMatcher = allocatedPattern.matcher(line);
                    if (aMatcher.find() && currentThread != null) {
                        double val = Double.parseDouble(aMatcher.group(1));
                        String unit = aMatcher.group(2) != null ? aMatcher.group(2).toUpperCase() : "BYTES";

                        long bytes = (long) val;
                        if (unit.equals("KB")) bytes = (long) (val * 1024);
                        else if (unit.equals("MB")) bytes = (long) (val * 1024 * 1024);
                        else if (unit.equals("GB")) bytes = (long) (val * 1024 * 1024 * 1024);

                        threadMap.put(currentThread, bytes);
                    }
                }
                process.waitFor();

                // Add this specific benchmark's memory to the total project memory
                for (long bytes : threadMap.values()) {
                    totalAllocatedBytes += bytes;
                }

            } catch (Exception e) {
                System.err.println("Error parsing JFR file (" + jfrFile.getName() + "): " + e.getMessage());
            }
        }

        return totalAllocatedBytes / (1024 * 1024); // Return Total MB across all benchmarks
    }

    // Helper: Recursively finds EVERY file named "profile.jfr" and adds it to a list
    private static void findAllJfrFiles(File dir, List<File> jfrFiles) {
        if (dir.getName().equals("profile.jfr")) {
            jfrFiles.add(dir);
            return;
        }
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    findAllJfrFiles(f, jfrFiles);
                }
            }
        }
    }
}