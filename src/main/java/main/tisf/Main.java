package main.tisf;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class Main extends JavaPlugin {

    private DatabaseManager databaseManager;
    private RemoteServerController remoteServerController;
    private BungeeCordNotifier bungeeCordNotifier;
    private boolean databaseEnabled;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        databaseEnabled = getConfig().getBoolean("database.enable", true);
        if (databaseEnabled) {
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();
        }
        remoteServerController = new RemoteServerController(this);
        bungeeCordNotifier = new BungeeCordNotifier(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(), this);
        new PlayerMonitor(this).runTaskTimer(this, 0L, 20L * 60); // 每 60 秒执行一次
    }

    @Override
    public void onDisable() {
        if (databaseEnabled && databaseManager != null) {
            databaseManager.disconnect();
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public RemoteServerController getRemoteServerController() {
        return remoteServerController;
    }

    public BungeeCordNotifier getBungeeCordNotifier() {
        return bungeeCordNotifier;
    }

    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }
}
