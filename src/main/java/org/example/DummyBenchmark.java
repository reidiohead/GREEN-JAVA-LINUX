package org.example;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole; // MUST IMPORT THIS
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DummyBenchmark {

    @Benchmark
    public void simulateLongMethodSmell(Blackhole bh) {
        double result = 0;
        // 100 million iterations ensures a massive, measurable wattage spike
        for (int i = 0; i < 100_000_000; i++) {
            result += Math.pow(i, 2) / Math.sqrt(i + 1);
        }

        // THE FIX: This forces the JVM to actually calculate the loop
        // because it thinks you need the 'result' for something important.
        bh.consume(result);
    }
}