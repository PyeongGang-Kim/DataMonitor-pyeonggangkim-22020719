package org.example.service;

public class SummaryResult {
    private final long total;
    private final long activeCount;
    private final long inactiveCount;
    private final long warningCount;
    private final double averageValue;

    public SummaryResult(long total, long activeCount, long inactiveCount, long warningCount, double averageValue) {
        this.total = total;
        this.activeCount = activeCount;
        this.inactiveCount = inactiveCount;
        this.warningCount = warningCount;
        this.averageValue = averageValue;
    }

    public long getTotal() { return total; }
    public long getActiveCount() { return activeCount; }
    public long getInactiveCount() { return inactiveCount; }
    public long getWarningCount() { return warningCount; }
    public double getAverageValue() { return averageValue; }
}
