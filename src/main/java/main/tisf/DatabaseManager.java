package main.tisf;

import org.bukkit.plugin.Plugin;
import java.sql.*;

public class DatabaseManager {

    private Connection connection;
    private final Plugin plugin;

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            String host = plugin.getConfig().getString("database.host");
            int port = plugin.getConfig().getInt("database.port");
            String dbName = plugin.getConfig().getString("database.name");
            String user = plugin.getConfig().getString("database.user");
            String password = plugin.getConfig().getString("database.password");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=UTC";
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);

            // 初始化累計統計數據
            initializeCumulativeStats();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public void initializeCumulativeStats() {
        try {
            String sql = "INSERT INTO cumulative_stats (id, total_cpu_load, total_memory_usage, data_points) VALUES (1, 0, 0, 0) ON DUPLICATE KEY UPDATE id=1";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCumulativeLoad(double cpuLoad, double memoryUsage) {
        try {
            String sql = "UPDATE cumulative_stats SET total_cpu_load = total_cpu_load + ?, total_memory_usage = total_memory_usage + ?, data_points = data_points + 1 WHERE id = 1";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setDouble(1, cpuLoad);
            stmt.setDouble(2, memoryUsage);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public AverageLoad getAverageLoad() {
        try {
            String sql = "SELECT total_cpu_load, total_memory_usage, data_points FROM cumulative_stats WHERE id = 1";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                double totalCpuLoad = rs.getDouble("total_cpu_load");
                double totalMemoryUsage = rs.getDouble("total_memory_usage");
                int dataPoints = rs.getInt("data_points");

                if (dataPoints == 0) dataPoints = 1; // 防止除以零

                double avgCpuLoad = totalCpuLoad / dataPoints;
                double avgMemoryUsage = totalMemoryUsage / dataPoints;

                return new AverageLoad(avgCpuLoad, avgMemoryUsage);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new AverageLoad(0.5, 0.5); // 默認值，防止空指針異常
    }

    public void logDataToDatabase(int playerCount, int serverCount, double cpuLoad, double memoryUsage) {
        try {
            String sql = "INSERT INTO server_stats (timestamp, player_count, server_count, cpu_load, memory_usage) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(2, playerCount);
            stmt.setInt(3, serverCount);
            stmt.setDouble(4, cpuLoad);
            stmt.setDouble(5, memoryUsage);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getServerCount() {
        try {
            String sql = "SELECT COUNT(*) FROM server_instances WHERE status = 'running'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // default 1
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("資料庫連接已關閉。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
