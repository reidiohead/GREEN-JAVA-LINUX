package org.example;

import java.util.Map;

public class DynamicRefactoringEngine {

    public static String generateInsight(
            String targetSmell, String projectName,
            double joules, double executionTime, double memoryMB,
            double weightTime, double weightMem,
            double eisScore, String absoluteTier, String relativeTier,
            String staticLine) {

        // PROFESSOR'S FILTER: If the smell doesn't have "dirty" (or is explicitly clean), return empty!
        String sLower = targetSmell.toLowerCase();
        if (!sLower.contains("dirty") &&
                (sLower.contains("clean") || sLower.contains("floor") || sLower.contains("ceiling") || sLower.contains("baseline"))) {
            return "";
        }

        StringBuilder insight = new StringBuilder();

        // 1. Determine primary driver
        boolean isCpuBound = weightTime >= weightMem;
        String driver = isCpuBound
                ? String.format("CPU-Bound (Time Weight: %.1f%%)", weightTime * 100)
                : String.format("Memory-Bound (Heap Weight: %.1f%%)", weightMem * 100);

        // 2. Fetch specific action dynamically populated with physical metrics AND EIS Score
        String action = generateSmellSpecificAction(targetSmell, isCpuBound, joules, executionTime, memoryMB, eisScore);
        String cleanRelativeTier = relativeTier.replace("_", " ").replace("Q", "Quartile ");

        // 3. Build the Restructured Authentic Format
        insight.append("\n==================================================\n");
        insight.append(" GREEN JAVA - INTEGRATED INSIGHT REPORT\n");
        insight.append("==================================================\n\n");

        insight.append("   SYNTHESIS (Green Java EIS)\n");
        insight.append(String.format("   EIS Score:         %.2f\n", eisScore));
        insight.append(String.format("   Absolute Rank:     [%s] (Global Hardware Scale)\n", absoluteTier));
        insight.append(String.format("   Relative Rank:     [%s] (Local Architecture Scale)\n", cleanRelativeTier));
        insight.append(String.format("   Primary Driver:    %s\n\n", driver));

        insight.append("   DYNAMIC PROFILING (Stream B)\n");
        insight.append(String.format("   Joules/op:    %.6f J\n", joules));
        insight.append(String.format("   Exec Time:    %.6f s\n", executionTime));
        insight.append(String.format("   Memory:       %.2f MB\n\n", memoryMB));

        insight.append("   STATIC ANALYSIS (Stream A)\n");
        insight.append(String.format("   Target Smell: %s\n", targetSmell));
        insight.append(String.format("   Location:     %s → %s()\n\n", projectName, staticLine));

        insight.append("   REFACTORING BLUEPRINT\n   ");
        insight.append(action.replace("\n", "\n   "));

        insight.append("\n==================================================\n");

        return insight.toString();
    }

    private static String generateSmellSpecificAction(String smell, boolean isCpuBound, double joules, double execTime, double memoryMB, double eis) {
        String s = smell.toLowerCase();

        // GOD CLASS
        if (s.contains("godclass") || s.contains("god_class") || s.contains("s1200") || s.contains("s114")) {
            return isCpuBound
                    ? String.format("Problem: Monolithic coupling creates heavy instruction cache pressure, driving execution to %.6f s and EIS to %.2f.\n" +
                                    "Suggestion: Decompose into Single-Responsibility components using Composition.\n" +
                                    "Result: Reduces the instruction set required per method call, reclaiming the %.2f EIS penalty via improved CPU branch prediction.", execTime, eis, eis)
                    : String.format("Problem: Object state bloating inflates heap allocation to %.2f MB, triggering frequent GC cycles and an EIS of %.2f.\n" +
                                    "Suggestion: Extract helper classes and utilize Flyweight patterns for shared state.\n" +
                                    "Result: Lowers baseline memory occupancy, eliminating the GC-related energy spikes currently inflating the %.2f EIS.", memoryMB, eis, eis);
        }

        // LONG METHOD
        if (s.contains("longmethod") || s.contains("long_method") || s.contains("s138")) {
            return isCpuBound
                    ? String.format("Problem: Method exceeds JIT compilation thresholds (bytecode limit), burning %.6f J/op with an EIS of %.2f.\n" +
                                    "Suggestion: Implement 'Extract Method' refactoring to break logic into smaller primitives.\n" +
                                    "Result: Enables JIT inlining and 'HotSpot' optimization, directly neutralizing the %.2f EIS bottleneck.", joules, eis, eis)
                    : String.format("Problem: High local variable density extends stack frame lifecycle to %.2f MB, elevating EIS to %.2f.\n" +
                                    "Suggestion: Reduce variable scope and partition logic to allow immediate object nullification.\n" +
                                    "Result: Shortens the 'Live Range' of objects, allowing the Young Gen GC to lower the current %.2f EIS.", memoryMB, eis, eis);
        }

        // DUPLICATE CODE
        if (s.contains("duplicated") || s.contains("duplicate") || s.contains("s1192")) {
            return isCpuBound
                    ? String.format("Problem: Redundant execution of identical logic paths wastes %.6f J/op, resulting in an EIS of %.2f.\n" +
                                    "Suggestion: Centralize logic into a utility or base class to leverage JVM method caching.\n" +
                                    "Result: Optimizes the CPU pipeline by avoiding redundant ALU operations, dropping the %.2f EIS.", joules, eis, eis)
                    : String.format("Problem: Redundant object instantiation occupies %.2f MB of redundant heap space, creating an EIS of %.2f.\n" +
                                    "Suggestion: Externalize constants and reuse object instances via a Singleton or Factory.\n" +
                                    "Result: Decreases the object graph size, significantly reducing the %.2f EIS memory overhead.", memoryMB, eis, eis);
        }

        // RESOURCE LEAK
        if (s.contains("resourceleak") || s.contains("resource_leak") || s.contains("s2095")) {
            return isCpuBound
                    ? String.format("Problem: Unclosed OS descriptors force the JVM to stall for %.6f s during cleanup, driving EIS to %.2f.\n" +
                                    "Suggestion: Wrap resource-heavy operations in try-with-resources blocks.\n" +
                                    "Result: Ensures deterministic cleanup and prevents CPU cycles from being wasted on zombie processes, lowering the %.2f EIS.", execTime, eis, eis)
                    : String.format("Problem: Leaked Stream/DB objects accumulate in the Old Gen (%.2f MB), forcing major STW GC events and a %.2f EIS.\n" +
                                    "Suggestion: Verify all Closeable implementations are terminated in a finally block.\n" +
                                    "Result: Restores heap stability and stops the 'Stop-The-World' events currently maintaining the %.2f EIS.", memoryMB, eis, eis);
        }

        // DATA CLUMP
        if (s.contains("dataclump") || s.contains("data_clump") || s.contains("s107")) {
            return isCpuBound
                    ? String.format("Problem: Excessive primitive parameters increase stack-to-register mapping time (%.6f s), causing a %.2f EIS.\n" +
                                    "Suggestion: Encapsulate related primitives into a single POJO/DTO.\n" +
                                    "Result: Improves spatial data locality, reducing the execution delay currently contributing to the %.2f EIS.", execTime, eis, eis)
                    : String.format("Problem: Fragmented primitive data prevents the JIT from performing Effective Escape Analysis, wasting %.6f J/op and inflating EIS to %.2f.\n" +
                                    "Suggestion: Bundle parameters to allow the JVM to perform Scalar Replacement.\n" +
                                    "Result: Enables stack-allocation of objects, bypassing the heap and lowering the %.2f EIS penalty.", joules, eis, eis);
        }

        return String.format("Problem: Unclassified structural decay consumes %.2f MB and %.6f J/op, resulting in a baseline EIS of %.2f.\n" +
                "Suggestion: Apply standard SOLID principles to improve computational efficiency.\n" +
                "Result: Expected optimization of system resources and a downward trend in the %.2f EIS.", memoryMB, joules, eis, eis);
    }
}