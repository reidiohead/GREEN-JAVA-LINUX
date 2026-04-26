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

        // 1. Find ALL profile.jfr files recursively in the project folder
        List<File> allJfrFiles = new ArrayList<>();
        findAllJfrFiles(new File(folderPath), allJfrFiles);

        if (allJfrFiles.isEmpty()) {
            System.err.println("JFR Parser: No profile.jfr files found in: " + folderPath);
            return 0.0;
        }

        System.out.println("JFR Parser: Found " + allJfrFiles.size() + " profile.jfr file(s).");

        // 2. Loop through every JFR file found
        for (File jfrFile : allJfrFiles) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "jfr", "print",
                        "--events", "jdk.ThreadAllocationStatistics",
                        jfrFile.getAbsolutePath()
                );
                pb.redirectErrorStream(true); // capture stderr too
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                // FIX: Use merge with Math::max so we always keep the
                // LARGEST (most recent cumulative) allocation per thread.
                // jdk.ThreadAllocationStatistics is emitted multiple times;
                // put() would randomly overwrite with an earlier snapshot.
                Map<String, Long> threadMap = new HashMap<>();

                Pattern threadPattern = Pattern.compile("thread = \"(.*?)\"");
                Pattern allocatedPattern = Pattern.compile(
                        "allocated = ([\\d\\.]+)\\s*(bytes|kB|MB|GB)?",
                        Pattern.CASE_INSENSITIVE);

                String line;
                String currentThread = null;

                while ((line = reader.readLine()) != null) {
                    Matcher tMatcher = threadPattern.matcher(line);
                    if (tMatcher.find()) {
                        currentThread = tMatcher.group(1);
                    }

                    Matcher aMatcher = allocatedPattern.matcher(line);
                    if (aMatcher.find() && currentThread != null) {
                        double val = Double.parseDouble(aMatcher.group(1));
                        String unit = aMatcher.group(2) != null
                                ? aMatcher.group(2).toUpperCase()
                                : "BYTES";

                        long bytes = (long) val;
                        if (unit.equals("KB"))       bytes = (long) (val * 1_024);
                        else if (unit.equals("MB"))  bytes = (long) (val * 1_024 * 1_024);
                        else if (unit.equals("GB"))  bytes = (long) (val * 1_024 * 1_024 * 1_024);

                        // FIX: merge keeps the MAX value per thread across
                        // multiple cumulative snapshots in the same JFR file
                        threadMap.merge(currentThread, bytes, Math::max);
                    }
                }

                process.waitFor();

                // Sum the final peak allocation of every thread in this file
                for (long bytes : threadMap.values()) {
                    totalAllocatedBytes += bytes;
                }

            } catch (Exception e) {
                System.err.println("Error parsing JFR file ("
                        + jfrFile.getName() + "): " + e.getMessage());
            }
        }

        // Return total MB across all benchmark JFR files
        return totalAllocatedBytes / (1024.0 * 1024.0);
    }

    /**
     * Recursively walks the directory tree and collects every file
     * named exactly "profile.jfr".
     *
     * FIX: Added isFile() guard so files that are NOT profile.jfr
     * are correctly skipped instead of falling through silently.
     */
    private static void findAllJfrFiles(File dir, List<File> jfrFiles) {
        if (dir.isFile()) {
            // It is a file — only add it if the name matches
            if (dir.getName().equals("profile.jfr")) {
                jfrFiles.add(dir);
            }
            return; // either way, stop here — files have no children
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