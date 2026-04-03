package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Green Java - Hybrid Analysis Orchestrator");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(900, 650);
        frame.setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton browseBtn = new JButton("Browse Project Folder");
        JLabel pathLabel = new JLabel("No folder selected...");

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        browseBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                pathLabel.setText(file.getAbsolutePath());
                resultArea.setText("Target Project: " + file.getAbsolutePath() + "\nSystem Controller Ready...\n");
            }
        });

        topPanel.add(browseBtn);
        topPanel.add(pathLabel);

        JPanel bottomPanel = new JPanel();
        JButton runHybridBtn = new JButton("Run Full Hybrid Analysis (Static + Dynamic)");
        runHybridBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        runHybridBtn.setBackground(new Color(46, 204, 113));
        runHybridBtn.setForeground(Color.WHITE);
        bottomPanel.add(runHybridBtn);

        runHybridBtn.addActionListener(e -> {
            String selectedPath = pathLabel.getText();
            if (selectedPath.equals("No folder selected...")) {
                JOptionPane.showMessageDialog(frame, "Please select a Java project folder first!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            resultArea.setText("=== INITIATING HYBRID ANALYSIS ENGINE ===\n");
            resultArea.append("Target: " + selectedPath + "\n\n");
            runHybridAnalysis(resultArea, new File(selectedPath));
        });

        frame.getContentPane().add(BorderLayout.NORTH, topPanel);
        frame.getContentPane().add(BorderLayout.CENTER, scrollPane);
        frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);

        frame.setVisible(true);
    }

    private static void runHybridAnalysis(JTextArea resultArea, File targetFolder) {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                String os = System.getProperty("os.name").toLowerCase();
                boolean isWin = os.contains("win");
                String currentDir = System.getProperty("user.dir");

                try {
                    // PHASE 1: STATIC ANALYSIS
                    publish("--------------------------------------------------\n");
                    publish("STREAM A: STATIC STRUCTURAL ANALYSIS\n");
                    publish("--------------------------------------------------\n");

                    String projectName = targetFolder.getName().replaceAll("[^a-zA-Z0-9_.-]", "_");
                    String dynamicProjectKey = "greenjava_" + projectName;
                    String sqToken = "squ_d802d4577d06f9be972233c1c26b42ca8166ec25";

                    java.util.List<String> sonarCmd = new java.util.ArrayList<>();
                    if (isWin) {
                        sonarCmd.add("cmd.exe"); sonarCmd.add("/c");
                        sonarCmd.add("D:\\SonarScanner\\sonar-scanner-cli-8.0.1.6346-windows-x64\\sonar-scanner-8.0.1.6346-windows-x64\\bin\\sonar-scanner.bat");
                    } else {
                        sonarCmd.add("sonar-scanner");
                    }
                    sonarCmd.add("-Dsonar.projectKey=" + dynamicProjectKey);
                    sonarCmd.add("-Dsonar.sources=.");
                    sonarCmd.add("-Dsonar.host.url=http://localhost:9000");
                    sonarCmd.add("-Dsonar.token=" + sqToken);
                    sonarCmd.add("-Dsonar.java.binaries=.");

                    ProcessBuilder sonarPb = new ProcessBuilder(sonarCmd);
                    sonarPb.directory(targetFolder);
                    sonarPb.redirectErrorStream(true);
                    Process sonarProcess = sonarPb.start();
                    BufferedReader sonarReader = new BufferedReader(new InputStreamReader(sonarProcess.getInputStream()));
                    String line;
                    while ((line = sonarReader.readLine()) != null) { publish(line + "\n"); }
                    sonarProcess.waitFor();

                    // PHASE 2: DYNAMIC PROFILING
                    publish("--------------------------------------------------\n");
                    publish("STREAM B: DYNAMIC EMPIRICAL ANALYSIS\n");
                    publish("--------------------------------------------------\n");

                    publish("1. Compiling JMH Executable JAR via Maven...\n");
                    java.util.List<String> mvnCmd = new java.util.ArrayList<>();
                    if (isWin) {
                        mvnCmd.add("cmd.exe"); mvnCmd.add("/c"); mvnCmd.add("mvn clean package -DskipTests");
                    } else {
                        mvnCmd.add("mvn clean package -DskipTests");
                    }
                    ProcessBuilder mvnPb = new ProcessBuilder(mvnCmd);
                    mvnPb.directory(targetFolder);
                    mvnPb.redirectErrorStream(true);
                    Process mvnProcess = mvnPb.start();
                    BufferedReader mvnReader = new BufferedReader(new InputStreamReader(mvnProcess.getInputStream()));
                    while ((line = mvnReader.readLine()) != null) { publish(line + "\n"); }

                    if (mvnProcess.waitFor() != 0) {
                        publish("\n[CRITICAL ERROR] Maven build failed.\n");
                        return null;
                    }

                    // --- NEW GREEN JAVA POWER PIPELINE ---
                    String joularCoreExePath = currentDir + "\\tools\\joularcore.exe";
                    PowerMonitor monitor = new PowerMonitor();

                    publish("\n2. Initializing Hardware Power Sensors...\n");

                    // --- THE NEW COOL-DOWN PHASE ---
                    publish("[GREEN JAVA] Cooling down CPU from Maven build (Waiting 5 seconds)...\n");
                    Thread.sleep(5000);

                    publish("[GREEN JAVA] Gathering 5-second Idle Baseline. Please do not move the mouse...\n");

                    monitor.startMonitor(joularCoreExePath);
                    Thread.sleep(5000); // 5 second baseline trap
                    double baselineWatts = monitor.stopAndGetAverage();
                    publish(String.format("[GREEN JAVA] Idle Baseline Established: %.2f W\n\n", baselineWatts));

                    publish("3. Launching JMH Treadmill...\n");
                    String joularJxPath = currentDir + "\\tools\\joularjx-3.1.0.jar";
                    java.util.List<String> runCmd = new java.util.ArrayList<>();

                    if (isWin) {
                        runCmd.add("cmd.exe");
                        runCmd.add("/c");
                        String fullCmd = "java -jar target\\benchmarks.jar " +
                                "-jvmArgs=\"-javaagent:\\\"" + joularJxPath + "\\\"\" " +
                                "-i 30 -wi 5 -f 1 -r 1s -w 1s -prof jfr";
                        runCmd.add(fullCmd);
                    }

                    // Start measuring active power BEFORE JMH starts
                    monitor.startMonitor(joularCoreExePath);

                    ProcessBuilder runPb = new ProcessBuilder(runCmd);
                    runPb.directory(targetFolder);
                    runPb.redirectErrorStream(true);
                    Process runProcess = runPb.start();

                    BufferedReader runReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                    double jmhScoreMs = 0.0;

                    while ((line = runReader.readLine()) != null) {
                        publish(line + "\n");
                        // Auto-extract the JMH score from the console output
                        if (line.contains("avgt") && line.contains("ms/op")) {
                            String[] parts = line.trim().split("\\s+");
                            for(int i = 0; i < parts.length; i++) {
                                if(parts[i].equals("avgt") && i + 2 < parts.length) {
                                    try {
                                        jmhScoreMs = Double.parseDouble(parts[i+2]);
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    }

                    int exitCode = runProcess.waitFor();

                    // Stop measuring the exact moment JMH finishes
                    double activeWatts = monitor.stopAndGetAverage();

                    if (exitCode == 0) {
                        publish("\n>>> DYNAMIC PROFILING COMPLETE!\n");

                        // --- FINAL EIS CALCULATION ---
                        double netWatts = activeWatts - baselineWatts;
                        if (netWatts < 0) netWatts = 0; // Prevent negative readings if baseline was skewed

                        double jmhTimeSeconds = jmhScoreMs / 1000.0;
                        double finalJoules = netWatts * jmhTimeSeconds;

                        publish("\n==================================================\n");
                        publish("⚡ GREEN JAVA - STREAM B: EMPIRICAL ANALYSIS RESULTS ⚡\n");
                        publish("==================================================\n");
                        publish(String.format("Gross Active Power: %.2f W\n", activeWatts));
                        publish(String.format("Idle System Noise: -%.2f W\n", baselineWatts));
                        publish(String.format("Net Code Power:     %.2f W\n", netWatts));
                        publish(String.format("Execution Time:     %.4f Seconds\n", jmhTimeSeconds));
                        publish("--------------------------------------------------\n");
                        publish(String.format("ENERGY GROUND TRUTH: %.4f Joules/op\n", finalJoules));
                        publish("==================================================\n");

                    } else {
                        publish("\n[ERROR] Dynamic profiling execution failed.\n");
                    }

                } catch (Exception ex) {
                    publish("\n[SYSTEM ERROR] " + ex.getMessage() + "\n");
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String text : chunks) {
                    resultArea.append(text);
                    resultArea.setCaretPosition(resultArea.getDocument().getLength());
                }
            }
        };
        worker.execute();
    }

    // --- INNER CLASS: HIDDEN POWER MONITOR ---
    public static class PowerMonitor {
        private Process joularProcess;
        private double totalWatts = 0;
        private int readingCount = 0;
        private boolean isMonitoring = false;

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
                        Pattern powerPattern = Pattern.compile("CPU\\s+([0-9.]+)\\s+W");

                        while (isMonitoring && (line = reader.readLine()) != null) {
                            // Strip ANSI color codes
                            String cleanLine = line.replaceAll("\u001B\\[[;\\d]*m", "");
                            Matcher matcher = powerPattern.matcher(cleanLine);
                            if (matcher.find()) {
                                double currentWatts = Double.parseDouble(matcher.group(1));
                                totalWatts += currentWatts;
                                readingCount++;
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

        public double stopAndGetAverage() {
            isMonitoring = false;
            if (joularProcess != null) {
                joularProcess.destroy();
            }
            if (readingCount == 0) return 0.0;
            return totalWatts / readingCount;
        }
    }
}