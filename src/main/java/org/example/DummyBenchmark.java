package org.example;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DummyBenchmark {

    // 1. Your original CPU-heavy benchmark
    @Benchmark
    public void simulateLongMethodSmell(Blackhole bh) {
        double result = 0;
        for (int i = 0; i < 100_000_000; i++) {
            result += Math.pow(i, 2) / Math.sqrt(i + 1);
        }
        bh.consume(result);
    }

    // 2. The NEW Memory-heavy benchmark
    @Benchmark
    public void simulateMemorySmell(Blackhole bh) {
        List<String> list = new ArrayList<>();
        // Creating 1 million strings will force the JVM to allocate heavy Heap memory
        for (int i = 0; i < 1_000_000; i++) {
            list.add(new String("Memory Allocation Test " + i));
        }
        bh.consume(list);
    }
}