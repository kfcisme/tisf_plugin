package main.tisf;
public class AverageLoad {
    private double avgCpuLoad;
    private double avgMemoryUsage;

    public AverageLoad(double avgCpuLoad, double avgMemoryUsage) {
        this.avgCpuLoad = avgCpuLoad;
        this.avgMemoryUsage = avgMemoryUsage;
    }

    public double getAvgCpuLoad() {
        return avgCpuLoad;
    }

    public double getAvgMemoryUsage() {
        return avgMemoryUsage;
    }
}
