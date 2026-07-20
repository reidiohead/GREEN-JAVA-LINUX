# 🌿 GreenJava
### Hybrid Java Code Smell Detection for CPU & Memory Utilization using Energy Inefficiency Score (EIS)

> **GreenJava** is a research-driven hybrid static and dynamic analysis framework that measures the energy inefficiency of Java applications. By combining code smell detection with runtime profiling, GreenJava introduces the **Energy Inefficiency Score (EIS)**—a quantitative metric that ranks Java code based on its impact on CPU utilization, memory consumption, and energy usage.

---

## 📖 Overview

Modern static analysis tools successfully identify code smells that affect maintainability, but they fail to answer an equally important question:

> **"How much energy does this code actually waste?"**

GreenJava bridges this gap by integrating **static code analysis** with **dynamic runtime profiling** to evaluate the real-world resource cost of Java source code.

Instead of merely reporting code smells, GreenJava correlates structural software defects with CPU usage, heap memory utilization, execution time, and hardware-level energy consumption to produce a unified **Energy Inefficiency Score (EIS).**

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

Each smell is analyzed using both structural and runtime evidence before generating its Energy Inefficiency Score.

---

# 🏗 System Architecture

GreenJava consists of two analysis pipelines.

## Stream A — Static Analysis

```text
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
