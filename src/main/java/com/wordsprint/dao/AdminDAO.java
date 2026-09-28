package com.wordsprint.dao;

import com.wordsprint.model.GameConfig;
import com.wordsprint.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDAO {

    public AdminDAO() {
        initSchema();
    }

    public void initSchema() {
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS game_config (
                    config_id INT PRIMARY KEY DEFAULT 1,
                    max_attempts INT NOT NULL DEFAULT 5,
                    max_daily_games INT NOT NULL DEFAULT 3,
                    game_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                    CONSTRAINT chk_single_config CHECK (config_id = 1)
                )
                """;

        String insertDefaultSql = """
                INSERT INTO game_config (config_id, max_attempts, max_daily_games, game_enabled)
                VALUES (1, 5, 3, TRUE)
                ON CONFLICT (config_id) DO NOTHING
                """;

        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps1 = con.prepareStatement(createTableSql)) {
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = con.prepareStatement(insertDefaultSql)) {
                    ps2.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public GameConfig getConfig() {
        String query = "SELECT max_attempts, max_daily_games, game_enabled FROM game_config WHERE config_id = 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null;
             ResultSet rs = ps != null ? ps.executeQuery() : null) {

            if (rs != null && rs.next()) {
                return new GameConfig(
                        rs.getInt("max_attempts"),
                        rs.getInt("max_daily_games"),
                        rs.getBoolean("game_enabled")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new GameConfig(5, 3, true);
    }

    public boolean updateConfig(GameConfig config) {
        String query = """
                UPDATE game_config
                SET max_attempts = ?, max_daily_games = ?, game_enabled = ?
                WHERE config_id = 1
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null) {

            if (con == null || ps == null) return false;

            ps.setInt(1, config.getMaxAttempts());
            ps.setInt(2, config.getMaxDailyGames());
            ps.setBoolean(3, config.isGameEnabled());

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Integer> getSystemStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalPlayers", 0);
        stats.put("totalAdmins", 0);
        stats.put("totalGames", 0);
        stats.put("wonGames", 0);
        stats.put("lostGames", 0);
        stats.put("inProgressGames", 0);

        String userStatsQuery = "SELECT role, COUNT(*) AS cnt FROM users GROUP BY role";
        String gameStatsQuery = "SELECT status, COUNT(*) AS cnt FROM games GROUP BY status";

        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(userStatsQuery);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String role = rs.getString("role");
                        int count = rs.getInt("cnt");
                        if ("player".equalsIgnoreCase(role)) {
                            stats.put("totalPlayers", count);
                        } else if ("admin".equalsIgnoreCase(role)) {
                            stats.put("totalAdmins", count);
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(gameStatsQuery);
                     ResultSet rs = ps.executeQuery()) {
                    int totalGames = 0;
                    while (rs.next()) {
                        String status = rs.getString("status");
                        int count = rs.getInt("cnt");
                        totalGames += count;
                        if ("WON".equalsIgnoreCase(status)) {
                            stats.put("wonGames", count);
                        } else if ("LOST".equalsIgnoreCase(status)) {
                            stats.put("lostGames", count);
                        } else if ("IN_PROGRESS".equalsIgnoreCase(status)) {
                            stats.put("inProgressGames", count);
                        }
                    }
                    stats.put("totalGames", totalGames);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    public List<Map<String, Object>> getPlayerReports() {
        List<Map<String, Object>> reports = new ArrayList<>();
        String query = """
                SELECT u.user_id, u.uname, u.role, u.created_at,
                       COUNT(g.game_id) AS total_games,
                       COUNT(CASE WHEN g.status = 'WON' THEN 1 END) AS wins,
                       COUNT(CASE WHEN g.status = 'LOST' THEN 1 END) AS losses,
                       COUNT(CASE WHEN g.status = 'IN_PROGRESS' THEN 1 END) AS in_progress
                FROM users u
                LEFT JOIN games g ON u.user_id = g.user_id
                GROUP BY u.user_id, u.uname, u.role, u.created_at
                ORDER BY u.created_at DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null;
             ResultSet rs = ps != null ? ps.executeQuery() : null) {

            if (rs != null) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", rs.getLong("user_id"));
                    map.put("username", rs.getString("uname"));
                    map.put("role", rs.getString("role"));
                    map.put("createdAt", rs.getTimestamp("created_at").toLocalDateTime().toString());
                    map.put("totalGames", rs.getInt("total_games"));
                    map.put("wins", rs.getInt("wins"));
                    map.put("losses", rs.getInt("losses"));
                    map.put("inProgress", rs.getInt("in_progress"));
                    reports.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reports;
    }

    public List<Map<String, Object>> getDailyReports() {
        List<Map<String, Object>> reports = new ArrayList<>();
        String query = """
                SELECT DATE(started_at) AS game_date,
                       COUNT(DISTINCT user_id) AS active_users,
                       COUNT(CASE WHEN status = 'WON' THEN 1 END) AS correct_guesses
                FROM games
                GROUP BY DATE(started_at)
                ORDER BY game_date DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null;
             ResultSet rs = ps != null ? ps.executeQuery() : null) {

            if (rs != null) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", rs.getDate("game_date") != null ? rs.getDate("game_date").toString() : "");
                    map.put("activeUsers", rs.getInt("active_users"));
                    map.put("correctGuesses", rs.getInt("correct_guesses"));
                    reports.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reports;
    }

    public List<Map<String, Object>> getUserDailyReports() {
        List<Map<String, Object>> reports = new ArrayList<>();
        String query = """
                SELECT u.uname,
                       DATE(g.started_at) AS game_date,
                       COUNT(DISTINCT g.game_id) AS words_tried,
                       COUNT(CASE WHEN g.status = 'WON' THEN 1 END) AS correct_guesses
                FROM games g
                JOIN users u ON g.user_id = u.user_id
                GROUP BY u.uname, DATE(g.started_at)
                ORDER BY game_date DESC, u.uname ASC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null;
             ResultSet rs = ps != null ? ps.executeQuery() : null) {

            if (rs != null) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("username", rs.getString("uname"));
                    map.put("date", rs.getDate("game_date") != null ? rs.getDate("game_date").toString() : "");
                    map.put("wordsTried", rs.getInt("words_tried"));
                    map.put("correctGuesses", rs.getInt("correct_guesses"));
                    reports.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reports;
    }
}
