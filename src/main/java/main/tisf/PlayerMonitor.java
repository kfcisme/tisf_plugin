package main.tisf;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerMonitor extends BukkitRunnable {

    private final Main plugin;
    private final LoadMonitor loadMonitor;
    private final DatabaseManager databaseManager;
    private final RemoteServerController remoteServerController;
    private final BungeeCordNotifier bungeeCordNotifier;
    private final PlayerListener playerListener;

    public PlayerMonitor(Main plugin) {
        this.plugin = plugin;
        this.loadMonitor = new LoadMonitor();
        this.databaseManager = plugin.getDatabaseManager();
        this.remoteServerController = plugin.getRemoteServerController();
        this.bungeeCordNotifier = plugin.getBungeeCordNotifier();
        this.playerListener = new PlayerListener();
    }

    @Override
    public void run() {
        int playerCount = Bukkit.getOnlinePlayers().size();
        double cpuLoad = loadMonitor.getCpuLoad();
        double memoryUsage = loadMonitor.getMemoryUsage();

        adjustServerInstances(playerCount, cpuLoad, memoryUsage);
    }

    private void adjustServerInstances(int playerCount, double cpuLoad, double memoryUsage) {
        if (plugin.isDatabaseEnabled()) {
            databaseManager.updateCumulativeLoad(cpuLoad, memoryUsage);
            AverageLoad averageLoad = databaseManager.getAverageLoad();

            double cpuLoadRatio = cpuLoad / averageLoad.getAvgCpuLoad();
            double memoryUsageRatio = memoryUsage / averageLoad.getAvgMemoryUsage();

            double dynamicThreshold = 1.5;

            if (cpuLoadRatio >= dynamicThreshold || memoryUsageRatio >= dynamicThreshold) {
                startNewServer();
            }
            if (shouldStopServer(cpuLoad, memoryUsage)) {
                stopIdleServer();
            }

            databaseManager.logDataToDatabase(playerCount, getTotalServerCount(), cpuLoad, memoryUsage);
        } else {
            if (shouldStopServer(cpuLoad, memoryUsage)) {
                stopIdleServer();
            }
        }
    }

    private void startNewServer() {
        String serverName = "server_" + System.currentTimeMillis();
        String address = "remote-host";
        int port = 25566;

        boolean started = remoteServerController.startServer(serverName, address, port);

        if (started) {
            bungeeCordNotifier.notifyAddServer(serverName, address, port);
            plugin.getLogger().info("New server is now working：" + serverName);
        } else {
            plugin.getLogger().warning("unable to run another server：" + serverName);
        }
    }

    private void stopIdleServer() {
        String serverName = "server_to_stop";

        boolean stopped = remoteServerController.stopServer(serverName);

        if (stopped) {
            bungeeCordNotifier.notifyRemoveServer(serverName);
            plugin.getLogger().info("Server has stop：" + serverName);
        } else {
            plugin.getLogger().warning("Unable to stop the server：" + serverName);
        }
    }

    private int getTotalServerCount() {
        if (plugin.isDatabaseEnabled()) {
            return databaseManager.getServerCount();
        } else {
            return 1;
        }
    }

    private boolean shouldStopServer(double cpuLoad, double memoryUsage) {
        boolean isIdle = playerListener.isServerIdle();

        if (isIdle && cpuLoad < 0.1 && memoryUsage < 0.1) {
            return true;
        }
        return false;
    }
}
