package org.example;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Green Java - Hybrid Analysis Orchestrator");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
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

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        JButton runHybridBtn = new JButton("1. Run Full Hybrid Analysis (Auto-Scoring)");
        runHybridBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        runHybridBtn.setBackground(new Color(46, 204, 113));
        runHybridBtn.setForeground(Color.WHITE);

        JButton runEisBtn = new JButton("2. Recalculate EIS Only");
        runEisBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        runEisBtn.setBackground(new Color(52, 152, 219));
        runEisBtn.setForeground(Color.WHITE);

        bottomPanel.add(runHybridBtn);
        bottomPanel.add(runEisBtn);

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

        runEisBtn.addActionListener(e -> {
            resultArea.append("\n=== MANUAL SCORING SERVICE TRIGGERED ===\n");
            resultArea.append(calculateAndGetEISReport());
            resultArea.setCaretPosition(resultArea.getDocument().getLength());
        });

        frame.getContentPane().add(BorderLayout.NORTH, topPanel);
        frame.getContentPane().add(BorderLayout.CENTER, scrollPane);
        frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);

        frame.setVisible(true);
    }

    // ==========================================
    // THE HYBRID ANALYSIS ENGINE
    // ==========================================
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

                    publish("Applying Green Java AST Instrumentation...\n");
                    instrumentTargetProject(targetFolder, true);

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

                    publish("[GREEN JAVA] Gathering 10-second Idle Baseline...\n");
                    monitor.startMonitor("");
                    Thread.sleep(10000);
                    double baselineWatts = monitor.stopAndGetAverage();
                    publish(String.format("[GREEN JAVA] Idle Baseline Established: %.2f W\n\n", baselineWatts));

                    publish("Sweeping lab environment...\n");
                    cleanOldJfrFolders(targetFolder);
                    publish("3. Launching JMH Treadmill...\n");

                    String joularJxPath = currentDir + (isWin ? "\\tools\\joularjx-3.1.0.jar" : "/tools/joularjx-3.1.0.jar");
                    java.util.List<String> runCmd = new java.util.ArrayList<>();

                    if (isWin) {
                        runCmd.add("cmd.exe");
                        runCmd.add("/c");
                        String fullCmd = "java -jar target\\benchmarks.jar -jvmArgs=\"-javaagent:\\\"" + joularJxPath + "\\\"\" -i 30 -wi 10 -f 1 -r 1s -w 1s -prof jfr";
                        runCmd.add(fullCmd);
                    } else {
                        runCmd.add("sh");
                        runCmd.add("-c");
                        String fullCmd = "java -jar target/benchmarks.jar -jvmArgs=\"-javaagent:" + joularJxPath + " -Djoularjx.sudo=false\" -i 30 -wi 10 -f 1 -r 1s -w 1s -prof jfr";
                        runCmd.add(fullCmd);
                    }

                    monitor.startMonitor("");

                    ProcessBuilder runPb = new ProcessBuilder(runCmd);
                    runPb.directory(targetFolder);
                    runPb.redirectErrorStream(true);
                    Process runProcess = runPb.start();

                    BufferedReader runReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                    double jmhScoreMs = 0.0;
                    String benchmarkMethod = "Unknown_Method";

                    while ((line = runReader.readLine()) != null) {
                        publish(line + "\n");
                        if (line.contains("avgt") && line.contains("ms/op") && !line.contains(":jfr")) {
                            String[] parts = line.trim().split("\\s+");
                            if (parts.length > 0) {
                                benchmarkMethod = parts[0];
                            }
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
                        if (netWatts < 0) netWatts = 0.01;

                        double jmhTimeSeconds = jmhScoreMs / 1000.0;
                        double finalJoules = netWatts * jmhTimeSeconds;

                        // Temporary static JFR parser call for memory - ensure your JfrParser class exists
                        double allocatedMemoryMB = 0.0;
                        try {
                            allocatedMemoryMB = JfrParser.extractMemoryAllocation(targetFolder.getAbsolutePath());
                        } catch (Exception e) {
                            publish("[WARNING] Failed to parse JFR memory. Defaulting to 0.\n");
                        }

                        publish("\n==================================================\n");
                        publish(" GREEN JAVA - STREAM B: EMPIRICAL ANALYSIS RESULTS \n");
                        publish("==================================================\n");
                        publish(String.format("Target Method:      %s\n", benchmarkMethod));
                        publish(String.format("Execution Time:     %.4f Seconds\n", jmhTimeSeconds));
                        publish(String.format("Cumulative Memory:  %.2f MB\n", allocatedMemoryMB));
                        publish(String.format("ENERGY GROUND TRUTH: %.4f Joules/op\n", finalJoules));
                        publish("==================================================\n");

                        // 1. SAVE TO RAW DATA LAKE (APPENDS TO RESULTS.CSV)
                        saveToCSV(projectName, benchmarkMethod, jmhTimeSeconds, allocatedMemoryMB, finalJoules);
                        publish("[SUCCESS] Results appended to Data Layer: results.csv\n");

                        // ==========================================
                        // NEW: AUTOMATIC EIS TRIGGER
                        // ==========================================
                        publish("\n--------------------------------------------------\n");
                        publish("AUTO-TRIGGERING SCORING SERVICE (DATA LAYER)\n");
                        publish("--------------------------------------------------\n");

                        // 2. TRIGGER THE MATH ENGINE AND DISPLAY (CREATES EIS_REPORT.CSV)
                        String eisReportText = calculateAndGetEISReport();
                        publish(eisReportText);

                    } else {
                        publish("\n[ERROR] Dynamic profiling execution failed.\n");
                    }

                } catch (Exception ex) {
                    publish("\n[SYSTEM ERROR] " + ex.getMessage() + "\n");
                } finally {
                    try {
                        instrumentTargetProject(targetFolder, false);
                        publish("[GREEN JAVA] Successfully cleaned up AST instrumentation.\n");
                    } catch (Exception e) {
                        publish("[WARNING] Failed to clean up instrumentation: " + e.getMessage() + "\n");
                    }
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

    // ==========================================
    // THE SCORING SERVICE CALCULATION LOGIC
    // ==========================================
    private static String calculateAndGetEISReport() {
        StringBuilder sb = new StringBuilder();
        File csvFile = new File(System.getProperty("user.dir"), "results.csv");

        if (!csvFile.exists()) {
            return "[ERROR] No results.csv found. Run Hybrid Analysis first.\n";
        }

        try {
            List<String> lines = Files.readAllLines(csvFile.toPath());
            // Need header + at least 3 rows to do proper spearman correlation
            if (lines.size() <= 3) {
                return "[WARNING] Not enough data points to calculate Spearman Correlation. Run more benchmarks to build the dataset!\n";
            }

            sb.append("1. Extracting Data Matrix from results.csv...\n");
            List<String[]> dataRows = new ArrayList<>();
            List<Double> times = new ArrayList<>();
            List<Double> memories = new ArrayList<>();
            List<Double> energies = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().isEmpty()) continue;
                String[] parts = lines.get(i).split(",");
                dataRows.add(parts);
                times.add(Double.parseDouble(parts[3]));
                memories.add(Double.parseDouble(parts[4]));
                energies.add(Double.parseDouble(parts[5]));
            }

            double minTime = times.stream().min(Double::compare).orElse(0.0);
            double maxTime = times.stream().max(Double::compare).orElse(1.0);
            double minMem = memories.stream().min(Double::compare).orElse(0.0);
            double maxMem = memories.stream().max(Double::compare).orElse(1.0);

            sb.append("2. Calculating Spearman Rank Correlation Weights...\n");
            double[] timeArr = times.stream().mapToDouble(d -> d).toArray();
            double[] memArr = memories.stream().mapToDouble(d -> d).toArray();
            double[] nrgArr = energies.stream().mapToDouble(d -> d).toArray();

            SpearmansCorrelation spearman = new SpearmansCorrelation();
            double timeCorr = Math.abs(spearman.correlation(timeArr, nrgArr));
            double memCorr = Math.abs(spearman.correlation(memArr, nrgArr));

            if (Double.isNaN(timeCorr)) timeCorr = 0.5;
            if (Double.isNaN(memCorr)) memCorr = 0.5;

            double totalCorr = timeCorr + memCorr;
            double weightTime = timeCorr / totalCorr;
            double weightMem = memCorr / totalCorr;

            sb.append(String.format("   -> Time Weight: %.2f%%\n", weightTime * 100));
            sb.append(String.format("   -> Memory Weight: %.2f%%\n", weightMem * 100));

            sb.append("3. Applying Min-Max Normalization & Weighted Sum Model...\n");

            // 3. WRITE THE CALCULATED EIS TO A SEPARATE FILE
            File reportFile = new File(System.getProperty("user.dir"), "eis_report.csv");
            try (PrintWriter out = new PrintWriter(new FileWriter(reportFile))) {
                out.println("Timestamp,TargetName,MethodName,NormTime,NormMemory,Final_EIS_Score(0-100)");

                for (String[] row : dataRows) {
                    double rawTime = Double.parseDouble(row[3]);
                    double rawMem = Double.parseDouble(row[4]);

                    double normTime = (maxTime == minTime) ? 0 : (rawTime - minTime) / (maxTime - minTime);
                    double normMem = (maxMem == minMem) ? 0 : (rawMem - minMem) / (maxMem - minMem);

                    double finalEIS = ((normTime * weightTime) + (normMem * weightMem)) * 100.0;

                    out.printf("%s,%s,%s,%.4f,%.4f,%.2f\n", row[0], row[1], row[2], normTime, normMem, finalEIS);
                    sb.append(String.format("   [%s] EIS: %.2f\n", row[2], finalEIS));
                }
            }
            sb.append("\n[SUCCESS] Final Report Generated: eis_report.csv\n");
            sb.append("The Energy Inefficiency Score is mathematically calibrated.\n");
            return sb.toString();

        } catch (Exception e) {
            return "\n[SYSTEM ERROR] Analytics Engine failed: " + e.getMessage() + "\n";
        }
    }

    private static void instrumentTargetProject(File targetFolder, boolean inject) throws Exception {
        try (Stream<Path> paths = Files.walk(targetFolder.toPath())) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            java.util.List<String> lines = Files.readAllLines(path);
                            java.util.List<String> newLines = new ArrayList<>();
                            boolean modified = false;
                            for (String line : lines) {
                                if (!inject && line.contains("// INJECTED BY GREEN JAVA ORCHESTRATOR")) {
                                    modified = true;
                                    continue;
                                }
                                newLines.add(line);
                                if (inject && line.trim().startsWith("@GreenBenchmark")) {
                                    newLines.add("    @org.openjdk.jmh.annotations.Benchmark // INJECTED BY GREEN JAVA ORCHESTRATOR");
                                    modified = true;
                                }
                            }
                            if (modified) Files.write(path, newLines);
                        } catch (IOException e) {
                            System.err.println("Instrumentation error on " + path + ": " + e.getMessage());
                        }
                    });
        }
    }

    private static void saveToCSV(String targetName, String methodName, double executionTime, double memoryMB, double joules) {
        try {
            File csvFile = new File(System.getProperty("user.dir"), "results.csv");
            boolean isNewFile = !csvFile.exists();
            try (PrintWriter out = new PrintWriter(new FileWriter(csvFile, true))) {
                if (isNewFile) {
                    out.println("Timestamp,TargetName,MethodName,ExecutionTime(s),CumulativeMemory(MB),Energy(Joules)");
                }
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                out.printf("%s,%s,%s,%.4f,%.2f,%.4f\n", timestamp, targetName, methodName, executionTime, memoryMB, joules);
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

    private static void cleanOldJfrFolders(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && (file.getName().endsWith("-AverageTime") || file.getName().equals("joularjx-result"))) {
                    deleteRecursively(file);
                }
            }
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}