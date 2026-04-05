package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
                    // PHASE 1: STATIC ANALYSIS (SonarQube)
                    publish("--------------------------------------------------\n");
                    publish("STREAM A: STATIC STRUCTURAL ANALYSIS\n");
                    publish("--------------------------------------------------\n");

                    String projectName = targetFolder.getName().replaceAll("[^a-zA-Z0-9_.-]", "_");
                    String dynamicProjectKey = "greenjava_" + projectName;
                    String sqToken = "squ_5801d173875ddfb825d7dd207498dc73372c1e2d";

                    java.util.List<String> sonarCmd = new java.util.ArrayList<>();
                    if (isWin) {
                        sonarCmd.add("cmd.exe"); sonarCmd.add("/c");
                        sonarCmd.add("D:\\SonarScanner\\sonar-scanner-cli-8.0.1.6346-windows-x64\\sonar-scanner-8.0.1.6346-windows-x64\\bin\\sonar-scanner.bat");
                    } else {
                        sonarCmd.add("/home/rei/Documents/GreenJava/BuildCode/sonar-scanner/sonar-scanner-8.0.1.6346-linux-x64/bin/sonar-scanner");
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
                        mvnCmd.add("mvn"); mvnCmd.add("clean"); mvnCmd.add("package"); mvnCmd.add("-DskipTests");
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

                    PowerMonitor monitor = new PowerMonitor();

                    publish("\n2. Initializing Hardware Power Sensors...\n");
                    publish("[GREEN JAVA] Cooling down CPU (Waiting 20 seconds)...\n");
                    Thread.sleep(20000);

                    publish("[GREEN JAVA] Gathering 10-second Idle Baseline. Please do not move the mouse...\n");
                    monitor.startMonitor("");
                    Thread.sleep(10000);
                    double baselineWatts = monitor.stopAndGetAverage();
                    publish(String.format("[GREEN JAVA] Idle Baseline Established: %.2f W\n\n", baselineWatts));

                    publish("3. Launching JMH Treadmill...\n");

                    String joularJxPath = currentDir + (isWin ? "\\tools\\joularjx-3.1.0.jar" : "/tools/joularjx-3.1.0.jar");
                    java.util.List<String> runCmd = new java.util.ArrayList<>();

                    if (isWin) {
                        runCmd.add("cmd.exe");
                        runCmd.add("/c");
                        String fullCmd = "java -jar target\\benchmarks.jar -jvmArgs=\"-javaagent:\\\"" + joularJxPath + "\\\"\" -i 30 -wi 5 -f 1 -r 1s -w 1s -prof jfr";
                        runCmd.add(fullCmd);
                    } else {
                        runCmd.add("sh");
                        runCmd.add("-c");
                        String fullCmd = "java -jar target/benchmarks.jar -jvmArgs=\"-javaagent:" + joularJxPath + " -Djoularjx.sudo=false\" -i 30 -wi 5 -f 1 -r 1s -w 1s -prof jfr";
                        runCmd.add(fullCmd);
                    }

                    monitor.startMonitor("");

                    ProcessBuilder runPb = new ProcessBuilder(runCmd);
                    runPb.directory(targetFolder);
                    runPb.redirectErrorStream(true);
                    Process runProcess = runPb.start();

                    BufferedReader runReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                    double jmhScoreMs = 0.0;

                    while ((line = runReader.readLine()) != null) {
                        publish(line + "\n");
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
                    double activeWatts = monitor.stopAndGetAverage();

                    if (exitCode == 0) {
                        publish("\n>>> DYNAMIC PROFILING COMPLETE!\n");

                        double netWatts = activeWatts - baselineWatts;
                        if (netWatts < 0) netWatts = 0.01; // Minimum floor so we don't get 0

                        double jmhTimeSeconds = jmhScoreMs / 1000.0;
                        double finalJoules = netWatts * jmhTimeSeconds;

                        publish("4. Extracting Internal Memory Profiling (JFR)...\n");
                        double allocatedMemoryMB = JfrParser.extractMemoryAllocation(targetFolder.getAbsolutePath());

                        publish("\n==================================================\n");
                        publish("⚡ GREEN JAVA - STREAM B: EMPIRICAL ANALYSIS RESULTS ⚡\n");
                        publish("==================================================\n");
                        publish(String.format("Gross Active Power: %.2f W\n", activeWatts));
                        publish(String.format("Idle System Noise: -%.2f W\n", baselineWatts));
                        publish(String.format("Net Code Power:     %.2f W\n", netWatts));
                        publish(String.format("Execution Time:     %.4f Seconds\n", jmhTimeSeconds));
                        publish(String.format("Cumulative Memory:  %.2f MB\n", allocatedMemoryMB));
                        publish("--------------------------------------------------\n");
                        publish(String.format("ENERGY GROUND TRUTH: %.4f Joules/op\n", finalJoules));
                        publish("==================================================\n");

                        // --- NEW CODE: APPEND TO CSV ---
                        saveToCSV(projectName, jmhTimeSeconds, allocatedMemoryMB, finalJoules);
                        publish("\n[SUCCESS] Results appended to Data Layer: results.csv\n");

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

    // --- CSV EXPORTER METHOD ---
    private static void saveToCSV(String targetName, double executionTime, double memoryMB, double joules) {
        try {
            File csvFile = new File(System.getProperty("user.dir"), "results.csv");
            boolean isNewFile = !csvFile.exists();

            try (PrintWriter out = new PrintWriter(new FileWriter(csvFile, true))) {
                // Change the CSV Header
                if (isNewFile) {
                    out.println("Timestamp,TargetName,ExecutionTime(s),CumulativeMemory(MB),Energy(Joules)");
                }
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                out.printf("%s,%s,%.4f,%.2f,%.4f\n", timestamp, targetName, executionTime, memoryMB, joules);
            }
        } catch (Exception e) {
            System.err.println("Failed to write to CSV: " + e.getMessage());
        }
    }

    public static class PowerMonitor {
        private final String RAPL_PATH = "/sys/class/powercap/intel-rapl:0/energy_uj";
        private long startMicroJoules = 0;
        private long startTimeNano = 0;

        private long readEnergyUj() {
            try (BufferedReader reader = new BufferedReader(new java.io.FileReader(RAPL_PATH))) {
                return Long.parseLong(reader.readLine().trim());
            } catch (Exception e) {
                System.err.println("RAPL Read Error: " + e.getMessage());
                return 0;
            }
        }

        public void startMonitor(String dummyPath) {
            startMicroJoules = readEnergyUj();
            startTimeNano = System.nanoTime();
        }

        public double stopAndGetAverage() {
            long endMicroJoules = readEnergyUj();
            long endTimeNano = System.nanoTime();

            if (startMicroJoules == 0 || endMicroJoules == 0) return 0.0;
            double elapsedSeconds = (endTimeNano - startTimeNano) / 1_000_000_000.0;
            double totalJoules = (endMicroJoules - startMicroJoules) / 1_000_000.0;
            if (elapsedSeconds <= 0) return 0.0;

            return totalJoules / elapsedSeconds;
        }
    }
}