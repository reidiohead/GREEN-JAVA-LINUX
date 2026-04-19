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
        frame.setSize(1000, 800);
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

        // Wrap it in a scroll pane with a nice border
        JScrollPane scrollPane = new JScrollPane(consolePane);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(98, 114, 164), 2));

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
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomPanel.setBackground(new Color(40, 42, 54));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton runHybridBtn = new JButton("1. Run Full Hybrid Analysis");
        runHybridBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        runHybridBtn.setBackground(new Color(80, 250, 123)); // Neon Green
        runHybridBtn.setForeground(new Color(30, 30, 30));
        runHybridBtn.setFocusPainted(false);

        JButton runEisBtn = new JButton("2. Recalculate EIS Data");
        runEisBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        runEisBtn.setBackground(new Color(139, 233, 253)); // Cyan
        runEisBtn.setForeground(new Color(30, 30, 30));
        runEisBtn.setFocusPainted(false);

        bottomPanel.add(runHybridBtn);
        bottomPanel.add(runEisBtn);

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

        frame.getContentPane().add(BorderLayout.NORTH, topPanel);
        frame.getContentPane().add(BorderLayout.CENTER, scrollPane);
        frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);

        frame.setVisible(true);
    }

    // ===================================================================================
    // THE SMART COLOR LOGGER (Parses text and applies colors dynamically)
    // ===================================================================================
    private static void logToConsole(JTextPane pane, String textBlock) {
        if (textBlock == null) return;

        // Split the block into individual lines, preserving the newline character
        String[] lines = textBlock.split("(?<=\n)");

        for (String line : lines) {
            Color c = new Color(248, 248, 242); // Default Dracula White

            // Keyword Color Routing
            if (line.contains("[RED]") || line.contains("ERROR") || line.contains("CRITICAL")) {
                c = new Color(255, 85, 85); // Bright Red
            } else if (line.contains("[YELLOW]") || line.contains("WARNING") || line.contains("YELLOW")) {
                c = new Color(241, 250, 140); // Bright Yellow
            } else if (line.contains("[GREEN]") || line.contains("SUCCESS") || line.contains("GREEN")) {
                c = new Color(80, 250, 123); // Neon Green
            } else if (line.contains("===") || line.contains("---")) {
                c = new Color(98, 114, 164); // Muted Blue for headers
            } else if (line.contains("GREEN JAVA")) {
                c = new Color(189, 147, 249); // Purple branding
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
            pane.setCaretPosition(doc.getLength()); // Auto-scroll to bottom
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // --- CONFIGURATION LOADER ---
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
            @Override
            protected Void doInBackground() {
                String os = System.getProperty("os.name").toLowerCase();
                boolean isWin = os.contains("win");
                String currentDir = System.getProperty("user.dir");

                Properties config = loadConfig();

                try {
                    publish("--------------------------------------------------\n");
                    publish("STREAM A: STATIC STRUCTURAL ANALYSIS\n");
                    publish("--------------------------------------------------\n");

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
                    while ((line = sonarReader.readLine()) != null) { publish(line + "\n"); }
                    sonarProcess.waitFor();

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
                    monitor.startMonitor();
                    Thread.sleep(10000);
                    double baselineWatts = monitor.stopAndGetAverage();
                    publish(String.format("[GREEN JAVA] Idle Baseline Established: %.2f W\n\n", baselineWatts));

                    publish("Sweeping lab environment...\n");
                    cleanOldJfrFolders(targetFolder);
                    publish("3. Launching JMH Treadmill...\n");

                    String joularJxPath = currentDir + (isWin ? "\\tools\\joularjx-3.1.0.jar" : "/tools/joularjx-3.1.0.jar");
                    java.util.List<String> runCmd = new java.util.ArrayList<>();

                    if (isWin) {
                        runCmd.add("cmd.exe"); runCmd.add("/c");
                        runCmd.add("java -jar target\\benchmarks.jar -jvmArgs=\"-javaagent:\\\"" + joularJxPath + "\\\"\" -i 30 -wi 10 -f 1 -r 1s -w 1s -prof jfr");
                    } else {
                        runCmd.add("sh"); runCmd.add("-c");
                        runCmd.add("java -jar target/benchmarks.jar -jvmArgs=\"-javaagent:" + joularJxPath + " -Djoularjx.sudo=false\" -i 30 -wi 10 -f 1 -r 1s -w 1s -prof jfr");
                    }

                    monitor.startMonitor();
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
                            if (parts.length > 0) benchmarkMethod = parts[0];

                            // ========================================================
                            // EXACT COLUMN PARSER (Targets 'Score', ignores 'Cnt')
                            // Format: [0:Benchmark] [1:Mode] [2:Cnt] [3:Score] [4:Error] [5:Units]
                            // ========================================================
                            if (parts.length >= 4) {
                                String scoreStr = parts[3];
                                if (scoreStr.equals("≈")) {
                                    jmhScoreMs = 0.000001; // Assign microscopic time for nanosecond ops
                                } else {
                                    try {
                                        jmhScoreMs = Double.parseDouble(scoreStr);
                                    } catch (Exception e) {
                                        jmhScoreMs = 0.000001; // Fallback
                                    }
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

                        double allocatedMemoryMB = 0.0;
                        try {
                            allocatedMemoryMB = JfrParser.extractMemoryAllocation(targetFolder.getAbsolutePath());
                        } catch (Exception e) {
                            publish("[WARNING] JFR Parsing issue: " + e.getMessage() + "\n");
                        }

                        String extractedSmell = extractSmellNameFromSource(targetFolder, benchmarkMethod);

                        publish("\n==================================================\n");
                        publish(" GREEN JAVA - STREAM B: EMPIRICAL ANALYSIS RESULTS \n");
                        publish("==================================================\n");
                        publish(String.format("Target Smell:       %s\n", extractedSmell));
                        publish(String.format("Target Method:      %s\n", benchmarkMethod));
                        publish(String.format("Gross Active Power: %.2f W\n", activeWatts));
                        publish(String.format("Idle System Noise:  %.2f W\n", baselineWatts));
                        publish(String.format("Net Code Power:     %.2f W\n", netWatts));
                        publish("--------------------------------------------------\n");
                        publish(String.format("Execution Time:     %.6f Seconds\n", jmhTimeSeconds));
                        publish(String.format("Cumulative Memory:  %.2f MB\n", allocatedMemoryMB));
                        publish("--------------------------------------------------\n");
                        publish(String.format("ENERGY GROUND TRUTH: %.6f Joules/op\n", finalJoules));
                        publish("==================================================\n");

                        saveToCSV(projectName, extractedSmell, benchmarkMethod, jmhTimeSeconds, allocatedMemoryMB, finalJoules);
                        publish("[SUCCESS] Results appended to Data Layer: results.csv\n");

                        publish("\n--------------------------------------------------\n");
                        publish("AUTO-TRIGGERING SCORING SERVICE (DATA LAYER)\n");
                        publish("--------------------------------------------------\n");
                        publish(calculateAndGetEISReport());

                    } else {
                        publish("\n[ERROR] Dynamic profiling execution failed.\n");
                    }
                } catch (Exception ex) {
                    publish("\n[CRITICAL ERROR] " + ex.getMessage() + "\n");
                } finally {
                    try {
                        instrumentTargetProject(targetFolder, false);
                        publish("[GREEN JAVA] Successfully cleaned up AST instrumentation.\n");
                    } catch (Exception e) {
                        publish("[WARNING] Cleanup failed: " + e.getMessage() + "\n");
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
        File csvFile = new File(System.getProperty("user.dir"), "results.csv");

        if (!csvFile.exists()) return "[ERROR] No results.csv found.\n";

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

            File reportFile = new File(System.getProperty("user.dir"), "eis_report.csv");
            Map<String, double[]> smellAggregator = new HashMap<>();

            try (PrintWriter out = new PrintWriter(new FileWriter(reportFile))) {
                out.println("Timestamp,ProjectName,TargetSmell,MethodName,NormTime,NormMemory,Final_EIS_Score,Category");

                for (String[] row : dataRows) {
                    String targetSmell = row[2];
                    String methodName = row[3];
                    double rawTime = Double.parseDouble(row[4]);
                    double rawMem = Double.parseDouble(row[5]);

                    double normTime = (maxTime == minTime) ? 0 : (rawTime - minTime) / (maxTime - minTime);
                    double normMem = (maxMem == minMem) ? 0 : (rawMem - minMem) / (maxMem - minMem);

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
                        // 1. Remove previously injected code during cleanup
                        if (!inject && line.contains("// INJECTED BY GREEN JAVA ORCHESTRATOR")) {
                            modified = true;
                            continue;
                        }

                        newLines.add(line);

                        // 2. Translate @GreenBenchmark into JMH Annotations
                        if (inject && line.trim().startsWith("@GreenBenchmark")) {

                            // Parse the custom API parameters, falling back to your defined defaults
                            int warmups = extractIntParameter(line, "warmupIterations", 10);
                            int measurements = extractIntParameter(line, "measurementIterations", 30);

                            // Inject the core Benchmark tag
                            newLines.add("    @org.openjdk.jmh.annotations.Benchmark // INJECTED BY GREEN JAVA ORCHESTRATOR");

                            // Dynamically inject the precise parameters the developer requested in your API
                            newLines.add("    @org.openjdk.jmh.annotations.Warmup(iterations = " + warmups + ", time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS) // INJECTED BY GREEN JAVA ORCHESTRATOR");
                            newLines.add("    @org.openjdk.jmh.annotations.Measurement(iterations = " + measurements + ", time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS) // INJECTED BY GREEN JAVA ORCHESTRATOR");

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

    // Helper method to parse integer parameters out of the @GreenBenchmark annotation string
    private static int extractIntParameter(String line, String paramName, int defaultValue) {
        if (!line.contains(paramName)) return defaultValue;
        try {
            int startIndex = line.indexOf(paramName) + paramName.length();
            int equalsIndex = line.indexOf("=", startIndex);
            if (equalsIndex == -1) return defaultValue;

            // Extract the string until the next comma or closing parenthesis
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
            File csvFile = new File(System.getProperty("user.dir"), "results.csv");
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