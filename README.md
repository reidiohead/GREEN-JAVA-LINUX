# 🌿 GreenJava
### Hybrid Java Code Smell Detection for CPU & Memory Utilization using Energy Inefficiency Score (EIS)

> **GreenJava** is a research-driven hybrid static and dynamic analysis framework that measures the energy inefficiency of Java applications. By combining code smell detection with runtime profiling, GreenJava introduces the **Energy Inefficiency Score (EIS)**—a quantitative metric that ranks Java code based on its impact on CPU utilization, memory consumption, and energy usage.

---

## 📖 Overview

Modern static analysis tools successfully identify code smells that affect maintainability, but they fail to answer an equally important question:

> **"How much energy does this code actually waste?"**

GreenJava bridges this gap by integrating **static code analysis** with **dynamic runtime profiling** to evaluate the real-world resource cost of Java source code.

Instead of merely reporting code smells, GreenJava correlates structural software defects with CPU usage, heap memory utilization, execution time, and hardware-level energy consumption to produce a unified **Energy Inefficiency Score (EIS).** :contentReference[oaicite:0]{index=0}

---

# ✨ Key Features

## 🔍 Static Analysis

- Detects Java code smells using SonarQube
- Parses Abstract Syntax Trees (AST)
- Supports automated code inspection
- Generates structural quality metrics

---

## ⚡ Dynamic Runtime Profiling

Measures real execution metrics using:

- CPU Execution Time
- Heap Memory Usage
- Energy Consumption (Joules)
- JVM Runtime Performance

Collected using:

- Java Microbenchmark Harness (JMH)
- Java Flight Recorder (JFR)
- JoularJX
- Intel RAPL Hardware Telemetry

---

## 📊 Energy Inefficiency Score (EIS)

GreenJava introduces the **Energy Inefficiency Score**, a weighted metric that combines static and runtime measurements into a single score representing software energy waste.

The framework uses:

- Min-Max Normalization
- Weighted Sum Model (WSM)
- Spearman Rank Correlation
- Quartile Severity Classification

The higher the EIS, the greater the resource inefficiency.

---

## 🧠 Intelligent Refactoring Suggestions

GreenJava provides context-aware recommendations based on detected code smells and runtime behavior.

Suggestions are generated for:

- CPU-intensive implementations
- Memory-heavy implementations
- Energy inefficient structures

---

# 🎯 Supported Code Smells

GreenJava currently analyzes five Java code smells:

- 🟥 God Class
- 📏 Long Method
- ♻️ Duplicate Code
- 📦 Data Clumps
- 💧 Resource Leak

Each smell is analyzed using both structural and runtime evidence before generating its Energy Inefficiency Score. :contentReference[oaicite:1]{index=1}

---

# 🏗 System Architecture

GreenJava consists of two analysis pipelines.

## Stream A — Static Analysis

```
Java Source Code
        │
        ▼
 SonarScanner CLI
        │
        ▼
   SonarQube Server
        │
        ▼
 Code Smell Detection
```

---

## Stream B — Dynamic Profiling

```
Java Source Code
        │
        ▼
Java Microbenchmark Harness
        │
        ▼
Java Flight Recorder
        │
        ▼
 JoularJX + Intel RAPL
        │
        ▼
Runtime Metrics
```

---

## Final Processing

```
Static Metrics
        +
Runtime Metrics
        │
        ▼
Normalization
        │
        ▼
Weighted Sum Model
        │
        ▼
Energy Inefficiency Score
        │
        ▼
Quartile Ranking
```

---

# 🚀 Technologies Used

## Programming Language

- Java 21

## Static Analysis

- SonarQube
- SonarScanner CLI

## Dynamic Profiling

- Java Microbenchmark Harness (JMH)
- Java Flight Recorder (JFR)
- JoularJX

## Hardware Telemetry

- Intel RAPL

## Build Tools

- Apache Maven

## IDE

- IntelliJ IDEA

## Operating System

- Ubuntu 24.04 LTS

---

# 📊 Output

GreenJava generates:

- 📈 Energy Inefficiency Score (EIS)
- 📉 CPU Usage
- 💾 Heap Memory Usage
- ⚡ Energy Consumption (Joules)
- 🏆 Quartile Ranking (Q1–Q4)
- 📄 Integrated Analysis Report
- 🛠 Refactoring Recommendations

---

# 📁 Project Structure

```
GreenJava
│
├── GreenJava-Orchestrator
├── Benchmark Projects
├── Static Analyzer
├── Dynamic Profiler
├── Data Layer
├── Generated Reports
├── Refactoring Engine
└── Documentation
```

---

# 🔬 Research Highlights

GreenJava was experimentally validated using:

- Hardware calibration benchmarks
- Controlled Java code smell experiments
- Real-world open-source Java repositories

Repositories included projects from:

- Apache Commons
- Apache PDFBox
- Jsoup
- CommonMark Java
- Google ZXing

The framework demonstrated a statistically significant positive correlation between Energy Inefficiency Score and actual measured energy consumption, validating EIS as a practical metric for prioritizing energy-aware refactoring. :contentReference[oaicite:2]{index=2}

---

# 🎓 Undergraduate Thesis

**Green Java: A Java Code Smell Detection for CPU and Memory Utilization: An Empirical Study Using EIS-Based Metrics**

Bachelor of Science in Computer Science

College of Engineering and Information Technology

Cavite State University – Main Campus

June 2026 :contentReference[oaicite:3]{index=3}

---

# 👨‍💻 Researchers

- **Francesca Louise A. De Guzman**
- **Jon Reinel M. Santiago**
- **Nicole Ann C. Teñoso** :contentReference[oaicite:4]{index=4}

---

# 👨‍🏫 Adviser

**Prof. Ezra Marie F. Ramos** :contentReference[oaicite:5]{index=5}

---

# 📸 Screenshots

```
/screenshots
├── dashboard.png
├── project-selection.png
├── eis-report.png
├── leaderboard.png
├── insight-report.png
└── quartile-ranking.png
```

---

# 🌱 Sustainable Development Goals

GreenJava contributes to:

- 🌍 **SDG 7 — Affordable and Clean Energy**
- ♻️ **SDG 12 — Responsible Consumption and Production**

By helping developers identify and eliminate energy-inefficient code, the framework promotes sustainable software engineering practices and greener computing. :contentReference[oaicite:6]{index=6}

---

# 📄 Citation

If you use GreenJava in academic work, please cite:

> De Guzman, F. L. A., Santiago, J. R. M., & Teñoso, N. A. C. (2026). *Green Java: A Java Code Smell Detection for CPU and Memory Utilization: An Empirical Study Using EIS-Based Metrics*. Cavite State University.

---

# 📜 License

This repository is intended for academic, research, and educational purposes.

© 2026 Francesca Louise A. De Guzman, Jon Reinel M. Santiago, and Nicole Ann C. Teñoso. All Rights Reserved.
