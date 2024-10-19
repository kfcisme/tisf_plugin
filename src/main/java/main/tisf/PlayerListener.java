package main.tisf;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private long lastActiveTime;

    public PlayerListener() {
        lastActiveTime = System.currentTimeMillis();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        lastActiveTime = System.currentTimeMillis();
    }

    public boolean isServerIdle() {
        return (System.currentTimeMillis() - lastActiveTime) >= (30 * 60 * 1000); // 30 分钟
    }
}
