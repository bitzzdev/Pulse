package io.github.pulse.performance;

import java.util.Arrays;

public class RollingStatistics {
    private final double[] samples;
    private final int windowSize;
    private int head;
    private int count;
    private double sum;
    private double sumSq;

    public RollingStatistics(int windowSize) {
        this.windowSize = windowSize;
        this.samples = new double[windowSize];
        this.head = 0;
        this.count = 0;
        this.sum = 0.0;
        this.sumSq = 0.0;
    }

    public void addSample(double value) {
        if (count == windowSize) {
            double removed = samples[head];
            sum -= removed;
            sumSq -= removed * removed;
        } else {
            count++;
        }
        samples[head] = value;
        sum += value;
        sumSq += value * value;
        head = (head + 1) % windowSize;
    }

    public double getAverage() {
        if (count == 0) return 0.0;
        return sum / count;
    }

    public double getMin() {
        if (count == 0) return 0.0;
        double min = Double.MAX_VALUE;
        int idx = head - count;
        if (idx < 0) idx += windowSize;
        for (int i = 0; i < count; i++) {
            double v = samples[idx];
            if (v < min) min = v;
            idx = (idx + 1) % windowSize;
        }
        return min;
    }

    public double getMax() {
        if (count == 0) return 0.0;
        double max = -Double.MAX_VALUE;
        int idx = head - count;
        if (idx < 0) idx += windowSize;
        for (int i = 0; i < count; i++) {
            double v = samples[idx];
            if (v > max) max = v;
            idx = (idx + 1) % windowSize;
        }
        return max;
    }

    public double getSum() {
        return sum;
    }

    public double getVariance() {
        if (count == 0) return 0.0;
        double mean = sum / count;
        double variance = (sumSq / count) - (mean * mean);
        return Math.max(0.0, variance);
    }

    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }

    public double getPercentile(double percentile) {
        if (count == 0) return 0.0;
        double[] sorted = new double[count];
        int idx = head - count;
        if (idx < 0) idx += windowSize;
        for (int i = 0; i < count; i++) {
            sorted[i] = samples[idx];
            idx = (idx + 1) % windowSize;
        }
        Arrays.sort(sorted);
        double rank = percentile / 100.0 * (count - 1);
        int lower = (int) Math.floor(rank);
        int upper = (int) Math.ceil(rank);
        if (lower == upper) return sorted[lower];
        return sorted[lower] + (rank - lower) * (sorted[upper] - sorted[lower]);
    }

    public int getCount() {
        return count;
    }

    public void clear() {
        head = 0;
        count = 0;
        sum = 0.0;
        sumSq = 0.0;
    }
}
