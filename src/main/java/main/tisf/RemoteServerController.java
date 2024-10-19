package main.tisf;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.graversen.minecraft.rcon.commands.base.ICommand;
import io.graversen.minecraft.rcon.service.ConnectOptions;
import io.graversen.minecraft.rcon.service.MinecraftRconService;
import io.graversen.minecraft.rcon.service.RconDetails;
import io.graversen.minecraft.rcon.commands.base.ICommand;
import io.graversen.minecraft.rcon.util.*;
import com.github.t9t.minecraftrconclient.RconClient;
import com.github.t9t.minecraftrconclient.*;
import io.graversen.minecraft.rcon.MinecraftRcon.*;
import io.graversen.minecraft.rcon.*;
import io.graversen.minecraft.rcon.MinecraftRcon.*;
import io.graversen.minecraft.rcon.*;
import io.graversen.minecraft.rcon.util.*;

import java.util.Optional;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;



public class RemoteServerController {

    private Main plugin;
    private String rconHost;
    private int rconPort;
    private String rconPassword;

    public RemoteServerController(Main plugin) {
        this.plugin = plugin;
        this.rconHost = plugin.getConfig().getString("rcon.host", "localhost");
        this.rconPort = plugin.getConfig().getInt("rcon.port", 25575);
        this.rconPassword = plugin.getConfig().getString("rcon.password", "your_rcon_password");
    }

    public boolean startServer(String serverName, String host, int port) {
        String sshHost = plugin.getConfig().getString("ssh.host", "localhost");
        String sshUser = plugin.getConfig().getString("ssh.user", "user");
        String sshPassword = plugin.getConfig().getString("ssh.password", "password");
        int sshPort = plugin.getConfig().getInt("ssh.port", 22);
        String startCommand = plugin.getConfig().getString("ssh.start_command", "bash /path/to/start_server.sh");

        String command = startCommand + " " + port;

        return executeRemoteCommand(sshHost, sshUser, sshPassword, sshPort, command);
    }

    public boolean stopServer(String serverName) {
        try {
            String rconHost = this.rconHost;
            int rconPort = this.rconPort;
            String rconPassword = this.rconPassword;

            RconDetails rconDetails = new RconDetails(rconHost, rconPort, rconPassword);

            MinecraftRconService minecraftRconService = new MinecraftRconService(
                    rconDetails,
                    ConnectOptions.defaults()
            );

            minecraftRconService.connectBlocking(Duration.ofSeconds(3));

            Optional<MinecraftRcon> optionalRcon = minecraftRconService.minecraftRcon();

            if (optionalRcon.isPresent()) {
                MinecraftRcon minecraftRcon = optionalRcon.get();

                final ICommand stopCommand = new ICommand() {
                    @Override
                    public String command() {
                        return "stop";
                    }
                };

                Future<RconResponse> responseFuture = minecraftRcon.sendAsync(stopCommand);

                String response = String.valueOf(responseFuture.get(3, TimeUnit.SECONDS));
                System.out.println("RCON response：" + response);

                minecraftRconService.disconnect();

                return true;
            } else {
                System.err.println("Unable to MinecraftRcon");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error when stopping server though RCON：" + e.getMessage());
            return false;
        }
    }

    private boolean executeRemoteCommand(String host, String user, String password, int port, String command) {
        JSch jsch = new JSch();
        try {
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);

            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);

            channelExec.setInputStream(null);

            InputStream in = channelExec.getInputStream();

            channelExec.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                plugin.getLogger().info("[RemoteCommand] " + line);
            }

            int exitStatus = channelExec.getExitStatus();

            channelExec.disconnect();
            session.disconnect();

            return exitStatus == 0;
        } catch (JSchException | IOException e) {
            e.printStackTrace();
            plugin.getLogger().warning("Failed to execute remote command：" + e.getMessage());
            return false;
        }
    }
}
