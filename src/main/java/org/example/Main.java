package org.example;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // --- 1. MODERN FRAME SETUP ---
        JFrame frame = new JFrame("Green Java - Hybrid Analysis Orchestrator");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1100, 800);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(40, 42, 54)); // Dracula Dark Theme

        // --- 2. TOP PANEL (FILE SELECTION) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(40, 42, 54));

        JButton browseBtn = new JButton("Browse Project Folder");
        browseBtn.setBackground(new Color(98, 114, 164));
        browseBtn.setForeground(Color.WHITE);
        browseBtn.setFocusPainted(false);

        JLabel pathLabel = new JLabel("No folder selected...");
        pathLabel.setForeground(new Color(248, 248, 242));
        pathLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));

        // --- 3. THE "HACKER TERMINAL" UPGRADE (JTextPane) ---
        JTextPane consolePane = new JTextPane();
        consolePane.setEditable(false);
        consolePane.setBackground(new Color(30, 30, 30)); // Deep terminal black
        consolePane.setFont(new Font("Monospaced", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(consolePane);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(98, 114, 164), 2));

        // INJECT LOGO ON BOOT
        appendColoredText(consolePane, GreenJavaLogo.getBootSequence(), new Color(80, 250, 123)); // Neon Green
        logToConsole(consolePane, "System Initialized. Awaiting target project selection...\n\n");

        browseBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                pathLabel.setText(file.getAbsolutePath());
                consolePane.setText(""); // Clear console
                logToConsole(consolePane, "Target Project: " + file.getAbsolutePath() + "\n");
                logToConsole(consolePane, "[SUCCESS] System Controller Ready...\n");
            }
        });

        topPanel.add(browseBtn);
        topPanel.add(pathLabel);

        // --- 4. BOTTOM PANEL (ACTION BUTTONS) ---
        JPanel bottomPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        bottomPanel.setBackground(new Color(40, 42, 54));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton runHybridBtn = new JButton("1. Run Hybrid Analysis");
        runHybridBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        runHybridBtn.setBackground(new Color(80, 250, 123)); // Neon Green
        runHybridBtn.setForeground(new Color(30, 30, 30));
        runHybridBtn.setFocusPainted(false);

        JButton runEisBtn = new JButton("2. Recalculate EIS Data");
        runEisBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        runEisBtn.setBackground(new Color(139, 233, 253)); // Cyan
        runEisBtn.setForeground(new Color(30, 30, 30));
        runEisBtn.setFocusPainted(false);

        JButton viewResultsBtn = new JButton("3. 📂 View CSV");
        viewResultsBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        viewResultsBtn.setBackground(new Color(255, 184, 108)); // Dracula Orange
        viewResultsBtn.setForeground(new Color(30, 30, 30));
        viewResultsBtn.setFocusPainted(false);

        JButton viewLogsBtn = new JButton("4. 📜 View Logs");
        viewLogsBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        viewLogsBtn.setBackground(new Color(189, 147, 249)); // Purple
        viewLogsBtn.setForeground(new Color(30, 30, 30));
        viewLogsBtn.setFocusPainted(false);

        bottomPanel.add(runHybridBtn);
        bottomPanel.add(runEisBtn);
        bottomPanel.add(viewResultsBtn);
        bottomPanel.add(viewLogsBtn);

        // --- 5. BUTTON ACTIONS ---
        runHybridBtn.addActionListener(e -> {
            String selectedPath = pathLabel.getText();
            if (selectedPath.equals("No folder selected...")) {
                JOptionPane.showMessageDialog(frame, "Please select a Java project folder first!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            consolePane.setText("");
            logToConsole(consolePane, "=== INITIATING HYBRID ANALYSIS ENGINE ===\n");
            logToConsole(consolePane, "Target: " + selectedPath + "\n\n");
            runHybridAnalysis(consolePane, new File(selectedPath));
        });

        runEisBtn.addActionListener(e -> {
            logToConsole(consolePane, "\n=== MANUAL SCORING SERVICE TRIGGERED ===\n");
            logToConsole(consolePane, calculateAndGetEISReport());
        });

        viewResultsBtn.addActionListener(e -> {
            File resultsDir = new File(System.getProperty("user.dir"), "results");
            if (!resultsDir.exists()) resultsDir.mkdirs();
            openDirectory(frame, resultsDir);
        });

        viewLogsBtn.addActionListener(e -> {
            File logsDir = new File(System.getProperty("user.dir") + File.separator + "results" + File.separator + "logs");
            if (!logsDir.exists()) logsDir.mkdirs();
            openDirectory(frame, logsDir);
        });

        frame.getContentPane().add(BorderLayout.NORTH, topPanel);
        frame.getContentPane().add(BorderLayout.CENTER, scrollPane);
        frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);

        frame.setVisible(true);
    }

    private static void openDirectory(JFrame frame, File dir) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(dir);
            } else {
                Runtime.getRuntime().exec("xdg-open " + dir.getAbsolutePath());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Failed to open directory. Error: " + ex.getMessage(), "Directory Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================================================================================
    // THE SMART COLOR LOGGER
    // ===================================================================================
    private static void logToConsole(JTextPane pane, String textBlock) {
        if (textBlock == null) return;
        String[] lines = textBlock.split("(?<=\n)");
        for (String line : lines) {
            Color c = new Color(248, 248, 242);
            if (line.contains("[RED]") || line.contains("ERROR") || line.contains("CRITICAL")) {
                c = new Color(255, 85, 85);
            } else if (line.contains("[YELLOW]") || line.contains("WARNING") || line.contains("YELLOW")) {
                c = new Color(241, 250, 140);
            } else if (line.contains("[GREEN]") || line.contains("SUCCESS") || line.contains("GREEN")) {
                c = new Color(80, 250, 123);
            } else if (line.contains("===") || line.contains("---")) {
                c = new Color(98, 114, 164);
            } else if (line.contains("GREEN JAVA")) {
                c = new Color(189, 147, 249);
            }
            appendColoredText(pane, line, c);
        }
    }

    private static void appendColoredText(JTextPane pane, String text, Color color) {
        StyledDocument doc = pane.getStyledDocument();
        Style style = pane.addStyle("ColorStyle", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text, style);
            pane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private static Properties loadConfig() {
        Properties prop = new Properties();
        File configFile = new File(System.getProperty("user.dir") + File.separator + "tools" + File.separator + "config.properties");
        try (FileInputStream input = new FileInputStream(configFile)) {
            prop.load(input);
        } catch (IOException ex) {
            System.err.println("[WARNING] Could not find tools/config.properties. Using default fallbacks.");
        }
        return prop;
    }

    private static void runHybridAnalysis(JTextPane consolePane, File targetFolder) {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            StringBuilder sessionLog = new StringBuilder();
            String benchmarkMethod = "Unknown_Method";

            private void publishLog(String msg) {
                sessionLog.append(msg);
                publish(msg);
            }

            @Override
            protected Void doInBackground() {
                String os = System.getProperty("os.name").toLowerCase();
                boolean isWin = os.contains("win");
                String currentDir = System.getProperty("user.dir");
                Properties config = loadConfig();

                try {
                    publishLog("--------------------------------------------------\n");
                    publishLog("STREAM A: STATIC STRUCTURAL ANALYSIS\n");
                    publishLog("--------------------------------------------------\n");

                    String projectName = targetFolder.getName().replaceAll("[^a-zA-Z0-9_.-]", "_");
                    String dynamicProjectKey = "greenjava_" + projectName;
                    String sqToken = config.getProperty("sonar.token", "YOUR_TOKEN_HERE");
                    String sqUrl = config.getProperty("sonar.host.url", "http://localhost:9000");

                    java.util.List<String> sonarCmd = new java.util.ArrayList<>();
                    if (isWin) {
                        sonarCmd.add("cmd.exe"); sonarCmd.add("/c");
                        sonarCmd.add(config.getProperty("sonar.scanner.windows", "sonar-scanner.bat"));
                    } else {
                        sonarCmd.add(config.getProperty("sonar.scanner.linux", "sonar-scanner"));
                    }
                    sonarCmd.add("-Dsonar.projectKey=" + dynamicProjectKey);
                    sonarCmd.add("-Dsonar.sources=.");
                    sonarCmd.add("-Dsonar.host.url=" + sqUrl);
                    sonarCmd.add("-Dsonar.token=" + sqToken);
                    sonarCmd.add("-Dsonar.java.binaries=.");

                    ProcessBuilder sonarPb = new ProcessBuilder(sonarCmd);
                    sonarPb.directory(targetFolder);
                    sonarPb.redirectErrorStream(true);
                    Process sonarProcess = sonarPb.start();
                    BufferedReader sonarReader = new BufferedReader(new InputStreamReader(sonarProcess.getInputStream()));
                    String line;
                    while ((line = sonarReader.readLine()) != null) { publishLog(line + "\n"); }
                    sonarProcess.waitFor();

                    publishLog("--------------------------------------------------\n");
                    publishLog("STREAM B: DYNAMIC EMPIRICAL ANALYSIS\n");
                    publishLog("--------------------------------------------------\n");

                    publishLog("Applying Green Java AST Instrumentation...\n");
                    instrumentTargetProject(targetFolder, true);

                    publishLog("1. Compiling JMH Executable JAR via Maven...\n");
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
                    while ((line = mvnReader.readLine()) != null) { publishLog(line + "\n"); }

                    if (mvnProcess.waitFor() != 0) {
                        publishLog("\n[CRITICAL ERROR] Maven build failed.\n");
                        return null;
                    }

                    PowerMonitor monitor = new PowerMonitor();
                    publishLog("\n2. Initializing Hardware Power Sensors...\n");
                    publishLog("[GREEN JAVA] Cooling down CPU (Waiting 20 seconds)...\n");
                    Thread.sleep(20000);

                    publishLog("[GREEN JAVA] Gathering 10-second Idle Baseline...\n");
                    monitor.startMonitor();
                    Thread.sleep(10000);
                    double baselineWatts = monitor.stopAndGetAverage();
                    publishLog(String.format("[GREEN JAVA] Idle Baseline Established: %.2f W\n\n", baselineWatts));

                    publishLog("Sweeping lab environment...\n");
                    cleanOldJfrFolders(targetFolder);
                    publishLog("3. Launching JMH Treadmill...\n");

                    String joularJxPath = currentDir + (isWin ? "\\tools\\joularjx-3.1.0.jar" : "/tools/joularjx-3.1.0.jar");
                    java.util.List<String> runCmd = new java.util.ArrayList<>();

                    if (isWin) {
                        runCmd.add("cmd.exe"); runCmd.add("/c");
                        runCmd.add("java -jar target\\benchmarks.jar -jvmArgs=\"-javaagent:\\\"" + joularJxPath + "\\\"\" -r 1s -w 1s -prof jfr");
                    } else {
                        runCmd.add("sh"); runCmd.add("-c");
                        runCmd.add("java -jar target/benchmarks.jar -jvmArgs=\"-javaagent:" + joularJxPath + " -Djoularjx.sudo=false\" -r 1s -w 1s -prof jfr");
                    }

                    monitor.startMonitor();
                    long jmhStartNano = System.nanoTime();

                    ProcessBuilder runPb = new ProcessBuilder(runCmd);
                    runPb.directory(targetFolder);
                    runPb.redirectErrorStream(true);
                    Process runProcess = runPb.start();

                    BufferedReader runReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));

                    double jmhScoreMs = 0.0;

                    while ((line = runReader.readLine()) != null) {
                        publishLog(line + "\n");
                        if (line.contains("avgt") && line.contains("ms/op") && !line.contains(":jfr")) {
                            String[] parts = line.trim().split("\\s+");
                            if (parts.length > 0) benchmarkMethod = parts[0];

                            if (parts.length >= 5) {
                                String candidatePrimary   = parts[4];
                                String candidateFallback  = parts[3];
                                String scoreStr = isNumeric(candidatePrimary) ? candidatePrimary : candidateFallback;

                                if (scoreStr.equals("\u2248")) {
                                    jmhScoreMs = 0.000001;
                                } else {
                                    try { jmhScoreMs = Double.parseDouble(scoreStr); }
                                    catch (Exception e) { jmhScoreMs = 0.000001; }
                                }
                            } else if (parts.length >= 4) {
                                String scoreStr = parts[3];
                                if (scoreStr.equals("\u2248")) {
                                    jmhScoreMs = 0.000001;
                                } else {
                                    try { jmhScoreMs = Double.parseDouble(scoreStr); }
                                    catch (Exception e) { jmhScoreMs = 0.000001; }
                                }
                            }
                        }
                    }

                    int exitCode = runProcess.waitFor();
                    long jmhEndNano = System.nanoTime();
                    double jmhElapsedSeconds = (jmhEndNano - jmhStartNano) / 1_000_000_000.0;

                    double activeWatts = monitor.stopAndGetAverage();

                    if (exitCode == 0) {
                        publishLog("\n>>> DYNAMIC PROFILING COMPLETE!\n");

                        double netWatts = activeWatts - baselineWatts;
                        if (netWatts < 0) netWatts = 0.01;

                        // =========================================================
                        // FIX 1: MATHEMATICAL PHYSICS FIX
                        // We use the Score (execution time per operation) to
                        // find the Joules required for one algorithmic run.
                        // =========================================================
                        double jmhScoreSeconds = jmhScoreMs / 1000.0;
                        double finalJoules = netWatts * jmhScoreSeconds; // <-- RESTORED AND FIXED!

                        double allocatedMemoryMB = 0.0;
                        try {
                            allocatedMemoryMB = JfrParser.extractMemoryAllocation(targetFolder.getAbsolutePath());
                        } catch (Exception e) {
                            publishLog("[WARNING] JFR Parsing issue: " + e.getMessage() + "\n");
                        }

                        String extractedSmell = extractSmellNameFromSource(targetFolder, benchmarkMethod);

                        publishLog("\n==================================================\n");
                        publishLog(" GREEN JAVA - STREAM B: EMPIRICAL ANALYSIS RESULTS \n");
                        publishLog("==================================================\n");
                        publishLog(String.format("Target Smell:        %s\n", extractedSmell));
                        publishLog(String.format("Target Method:       %s\n", benchmarkMethod));
                        publishLog(String.format("Gross Active Power: %.2f W\n", activeWatts));
                        publishLog(String.format("Idle System Noise:  %.2f W\n", baselineWatts));
                        publishLog(String.format("Net Code Power:     %.2f W\n", netWatts));
                        publishLog("--------------------------------------------------\n");
                        publishLog(String.format("Avg Execution Time: %.6f s/op\n", jmhScoreSeconds));
                        publishLog(String.format("JMH Run Duration:   %.2f s (wall-clock)\n", jmhElapsedSeconds));
                        publishLog(String.format("Cumulative Memory:  %.2f MB\n", allocatedMemoryMB));
                        publishLog("--------------------------------------------------\n");
                        publishLog(String.format("ENERGY GROUND TRUTH: %.6f Joules/op\n", finalJoules));
                        publishLog("==================================================\n");

                        saveToCSV(projectName, extractedSmell, benchmarkMethod, jmhScoreSeconds, allocatedMemoryMB, finalJoules);
                        publishLog("[SUCCESS] Results appended to Data Layer: results/results.csv\n");

                        publishLog("\n--------------------------------------------------\n");
                        publishLog("AUTO-TRIGGERING SCORING SERVICE (DATA LAYER)\n");
                        publishLog("--------------------------------------------------\n");
                        publishLog(calculateAndGetEISReport());

                    } else {
                        publishLog("\n[ERROR] Dynamic profiling execution failed.\n");
                    }
                } catch (Exception ex) {
                    publishLog("\n[CRITICAL ERROR] " + ex.getMessage() + "\n");
                } finally {
                    try {
                        instrumentTargetProject(targetFolder, false);
                        publishLog("[GREEN JAVA] Successfully cleaned up AST instrumentation.\n");
                    } catch (Exception e) {
                        publishLog("[WARNING] Cleanup failed: " + e.getMessage() + "\n");
                    }

                    // --- 6. SAVE SESSION LOG TO TEXT FILE ---
                    try {
                        File logsDir = new File(System.getProperty("user.dir") + File.separator + "results" + File.separator + "logs");
                        if (!logsDir.exists()) logsDir.mkdirs();

                        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        String sanitizedMethodName = benchmarkMethod.replaceAll("[^a-zA-Z0-9_-]", "");
                        String logFileName = sanitizedMethodName + "_" + timestamp + ".txt";

                        File logFile = new File(logsDir, logFileName);
                        Files.writeString(logFile.toPath(), sessionLog.toString());
                    } catch (IOException e) {
                        System.err.println("Could not save session log: " + e.getMessage());
                    }
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String text : chunks) {
                    logToConsole(consolePane, text);
                }
            }
        };
        worker.execute();
    }

    private static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        try { Double.parseDouble(s); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private static String extractSmellNameFromSource(File targetFolder, String fullMethodName) {
        String[] parts = fullMethodName.split("\\.");
        if (parts.length == 0) return "Unclassified";
        String simpleMethodName = parts[parts.length - 1];

        try (Stream<Path> paths = Files.walk(targetFolder.toPath())) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                List<String> lines = Files.readAllLines(path);
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).contains("void " + simpleMethodName + "(")) {
                        for (int j = i - 1; j >= 0 && j >= i - 5; j--) {
                            String prevLine = lines.get(j);
                            if (prevLine.contains("@GreenBenchmark") && prevLine.contains("targetSmell")) {
                                int start = prevLine.indexOf("targetSmell");
                                int quote1 = prevLine.indexOf("\"", start);
                                int quote2 = prevLine.indexOf("\"", quote1 + 1);
                                if (quote1 != -1 && quote2 != -1) {
                                    return prevLine.substring(quote1 + 1, quote2);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing AST: " + e.getMessage());
        }
        return "Unclassified";
    }

    private static String calculateAndGetEISReport() {
        StringBuilder sb = new StringBuilder();
        File resultsDir = new File(System.getProperty("user.dir"), "results");
        if (!resultsDir.exists()) resultsDir.mkdirs();
        File csvFile = new File(resultsDir, "results.csv");

        if (!csvFile.exists()) return "[ERROR] No results/results.csv found.\n";

        try {
            List<String> lines = Files.readAllLines(csvFile.toPath());
            if (lines.size() <= 3) return "[WARNING] Not enough data points to calculate Spearman Correlation. Build the dataset!\n";

            sb.append("1. Extracting Data Matrix from results.csv...\n");
            List<String[]> dataRows = new ArrayList<>();
            List<Double> times = new ArrayList<>();
            List<Double> memories = new ArrayList<>();
            List<Double> energies = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().isEmpty()) continue;
                String[] parts = lines.get(i).split(",");
                dataRows.add(parts);
                times.add(Double.parseDouble(parts[4]));
                memories.add(Double.parseDouble(parts[5]));
                energies.add(Double.parseDouble(parts[6]));
            }

            // =========================================================================
            // FIX 2: GLOBAL HARDWARE CALIBRATION LIMITS
            // Hardcoded from Phase 1. Prevents the Min-Max denominator from shifting.
            // =========================================================================
            final double GLOBAL_MIN_TIME = 0.0;
            final double GLOBAL_MAX_TIME = 0.750; // Locked to Bubble Sort ceiling

            final double GLOBAL_MIN_MEM = 0.0;
            final double GLOBAL_MAX_MEM = 25000.0; // Locked to Merge Sort ceiling

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
            sb.append("3. Applying Min-Max Normalization (Anchored to Global Ceilings)...\n");

            File reportFile = new File(resultsDir, "eis_report.csv");
            Map<String, double[]> smellAggregator = new HashMap<>();

            try (PrintWriter out = new PrintWriter(new FileWriter(reportFile))) {
                out.println("Timestamp,ProjectName,TargetSmell,MethodName,NormTime,NormMemory,Final_EIS_Score,Category");

                for (String[] row : dataRows) {
                    String targetSmell = row[2];
                    String methodName = row[3];
                    double rawTime = Double.parseDouble(row[4]);
                    double rawMem = Double.parseDouble(row[5]);

                    // FIX APPLIED: Normalizing against absolute physical limits
                    double normTime = (rawTime - GLOBAL_MIN_TIME) / (GLOBAL_MAX_TIME - GLOBAL_MIN_TIME);
                    double normMem = (rawMem - GLOBAL_MIN_MEM) / (GLOBAL_MAX_MEM - GLOBAL_MIN_MEM);

                    // Safety cap to prevent scores over 100 or below 0
                    if (normTime > 1.0) normTime = 1.0;
                    if (normMem > 1.0) normMem = 1.0;
                    if (normTime < 0.0) normTime = 0.0;
                    if (normMem < 0.0) normMem = 0.0;

                    double finalEIS = ((normTime * weightTime) + (normMem * weightMem)) * 100.0;

                    String category = "GREEN";
                    if (finalEIS > 20.00) category = "YELLOW";
                    if (finalEIS > 60.00) category = "RED";

                    out.printf("%s,%s,%s,%s,%.4f,%.4f,%.2f,%s\n", row[0], row[1], targetSmell, methodName, normTime, normMem, finalEIS, category);

                    String shortMethod = methodName.substring(methodName.lastIndexOf(".") + 1);
                    sb.append(String.format("   [%-25s] EIS: %05.2f | [%s]\n", shortMethod, finalEIS, category));

                    if (!targetSmell.contains("Clean") && !targetSmell.equals("Unclassified") && !targetSmell.contains("Baseline")) {
                        smellAggregator.putIfAbsent(targetSmell, new double[]{0.0, 0});
                        smellAggregator.get(targetSmell)[0] += finalEIS;
                        smellAggregator.get(targetSmell)[1]++;
                    }
                }
            }

            if (!smellAggregator.isEmpty()) {
                List<AbstractMap.SimpleEntry<String, Double>> leaderboard = smellAggregator.entrySet().stream()
                        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()[0] / entry.getValue()[1]))
                        .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                        .collect(Collectors.toList());

                sb.append("\n==================================================\n");
                sb.append(" GREEN JAVA - EMPIRICAL CODE SMELL LEADERBOARD\n");
                sb.append("==================================================\n");
                int rank = 1;
                for (Map.Entry<String, Double> entry : leaderboard) {
                    double avgEis = entry.getValue();
                    String colorTag = (avgEis >= 60) ? "[RED]" : (avgEis >= 20 ? "[YELLOW]" : "[GREEN]");
                    sb.append(String.format("%d. %-30s | AVG EIS: %5.2f %s\n", rank, entry.getKey(), avgEis, colorTag));
                    rank++;
                }
                sb.append("==================================================\n");
            }
            sb.append("\nThe Energy Inefficiency Score is mathematically calibrated.\n");
            return sb.toString();
        } catch (Exception e) {
            return "\n[CRITICAL ERROR] Analytics Engine failed: " + e.getMessage() + "\n";
        }
    }

    private static void instrumentTargetProject(File targetFolder, boolean inject) throws Exception {
        try (Stream<Path> paths = Files.walk(targetFolder.toPath())) {
            paths.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".java")).forEach(path -> {
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
                            int warmups = extractIntParameter(line, "warmupIterations", 10);
                            int measurements = extractIntParameter(line, "measurementIterations", 30);
                            int forks = extractIntParameter(line, "forks", 1);

                            newLines.add("    @org.openjdk.jmh.annotations.Benchmark // INJECTED BY GREEN JAVA ORCHESTRATOR");
                            newLines.add("    @org.openjdk.jmh.annotations.Warmup(iterations = " + warmups + ", time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS) // INJECTED BY GREEN JAVA ORCHESTRATOR");
                            newLines.add("    @org.openjdk.jmh.annotations.Measurement(iterations = " + measurements + ", time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS) // INJECTED BY GREEN JAVA ORCHESTRATOR");
                            newLines.add("    @org.openjdk.jmh.annotations.Fork(value = " + forks + ") // INJECTED BY GREEN JAVA ORCHESTRATOR");
                            modified = true;
                        }
                    }
                    if (modified) Files.write(path, newLines);
                } catch (IOException e) {
                    System.err.println("Failed to instrument file: " + e.getMessage());
                }
            });
        }
    }

    private static int extractIntParameter(String line, String paramName, int defaultValue) {
        if (!line.contains(paramName)) return defaultValue;
        try {
            int startIndex = line.indexOf(paramName) + paramName.length();
            int equalsIndex = line.indexOf("=", startIndex);
            if (equalsIndex == -1) return defaultValue;

            int commaIndex = line.indexOf(",", equalsIndex);
            int parenIndex = line.indexOf(")", equalsIndex);
            int endIndex = (commaIndex != -1 && commaIndex < parenIndex) ? commaIndex : parenIndex;

            if (endIndex == -1) endIndex = line.length();

            String valStr = line.substring(equalsIndex + 1, endIndex).trim();
            return Integer.parseInt(valStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static void saveToCSV(String projectName, String targetSmell, String methodName, double executionTime, double memoryMB, double joules) {
        try {
            File resultsDir = new File(System.getProperty("user.dir"), "results");
            if (!resultsDir.exists()) resultsDir.mkdirs();

            File csvFile = new File(resultsDir, "results.csv");
            boolean isNewFile = !csvFile.exists();
            try (PrintWriter out = new PrintWriter(new FileWriter(csvFile, true))) {
                if (isNewFile) {
                    out.println("Timestamp,ProjectName,TargetSmell,MethodName,ExecutionTime(s),CumulativeMemory(MB),Energy(Joules)");
                }
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                out.printf("%s,%s,%s,%s,%.6f,%.2f,%.6f\n", timestamp, projectName, targetSmell, methodName, executionTime, memoryMB, joules);
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
                return 0;
            }
        }
        public void startMonitor() {
            startMicroJoules = readEnergyUj();
            startTimeNano = System.nanoTime();
        }
        public double stopAndGetAverage() {
            long endMicroJoules = readEnergyUj(); long endTimeNano = System.nanoTime();
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
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}