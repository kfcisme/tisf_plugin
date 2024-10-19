package main.tisf;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class LoadMonitor {

    private OperatingSystemMXBean osBean;

    public LoadMonitor() {
        osBean = ManagementFactory.getOperatingSystemMXBean();
    }

    public double getCpuLoad() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return sunOsBean.getSystemCpuLoad();
        } else {
            return -1;
        }
    }

    public double getMemoryUsage() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            long freeMemory = sunOsBean.getFreePhysicalMemorySize();
            long totalMemory = sunOsBean.getTotalPhysicalMemorySize();
            return 1 - ((double) freeMemory / totalMemory);
        } else {
            return -1;
        }
    }
}
