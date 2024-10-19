package main.tisf;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class BungeeCordNotifier {

    private Main plugin;
    private String bungeeHost;
    private int bungeePort;

    public BungeeCordNotifier(Main plugin) {
        this.plugin = plugin;
        this.bungeeHost = plugin.getConfig().getString("bungee.host", "localhost");
        this.bungeePort = plugin.getConfig().getInt("bungee.port", 25570);
    }

    public void notifyAddServer(String serverName, String address, int port) {
        sendMessage("add;" + serverName + ";" + address + ";" + port);
    }

    public void notifyRemoveServer(String serverName) {
        sendMessage("remove;" + serverName);
    }

    private void sendMessage(String message) {
        try (Socket socket = new Socket(bungeeHost, bungeePort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(message);
            plugin.getLogger().info("已通知 BungeeCord：" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
