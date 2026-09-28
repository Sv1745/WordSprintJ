package com.wordsprint.dao;

import com.wordsprint.model.Game;
import com.wordsprint.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class GameDAO {

    public boolean createGame(Game game) {

        String query = """
                INSERT INTO games
                (user_id, word_id, started_at, status)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS) : null) {

            if (con == null || ps == null) {
                return false;
            }

            ps.setLong(1, game.getUserId());
            ps.setLong(2, game.getWordId());
            ps.setTimestamp(
                    3,
                    Timestamp.valueOf(game.getStartedAt())
            );
            ps.setString(4, game.getStatus());

            if (ps.executeUpdate() != 1) {
                return false;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    game.setGameId(rs.getLong(1));
                    return true;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Game findById(Long gameId) {

        String query = """
                SELECT game_id, user_id, word_id,
                       started_at, completed_at, status
                FROM games
                WHERE game_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null) {

            if (con == null || ps == null) {
                return null;
            }

            ps.setLong(1, gameId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Game game = new Game();

                    game.setGameId(rs.getLong("game_id"));
                    game.setUserId(rs.getLong("user_id"));
                    game.setWordId(rs.getLong("word_id"));

                    game.setStartedAt(
                            rs.getTimestamp("started_at")
                                    .toLocalDateTime()
                    );

                    Timestamp completedAt =
                            rs.getTimestamp("completed_at");

                    if (completedAt != null) {
                        game.setCompletedAt(
                                completedAt.toLocalDateTime()
                        );
                    }

                    game.setStatus(rs.getString("status"));

                    return game;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateGameStatus(
            Long gameId,
            String status
    ) {

        String query = """
                UPDATE games
                SET completed_at = ?, status = ?
                WHERE game_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null) {

            if (con == null || ps == null) {
                return false;
            }

            ps.setTimestamp(
                    1,
                    Timestamp.valueOf(LocalDateTime.now())
            );
            ps.setString(2, status);
            ps.setLong(3, gameId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int getGamesCountToday(Long userId) {
        String query = """
                SELECT COUNT(*)
                FROM games
                WHERE user_id = ? AND started_at >= CURRENT_DATE
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null) {

            if (con == null || ps == null) {
                return 0;
            }

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public java.util.List<Game> findGamesByUserId(Long userId) {
        java.util.List<Game> list = new java.util.ArrayList<>();
        String query = """
                SELECT game_id, user_id, word_id, started_at, completed_at, status
                FROM games
                WHERE user_id = ?
                ORDER BY started_at DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null) {

            if (con == null || ps == null) return list;

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Game game = new Game();
                    game.setGameId(rs.getLong("game_id"));
                    game.setUserId(rs.getLong("user_id"));
                    game.setWordId(rs.getLong("word_id"));
                    game.setStartedAt(rs.getTimestamp("started_at").toLocalDateTime());

                    Timestamp completedAt = rs.getTimestamp("completed_at");
                    if (completedAt != null) {
                        game.setCompletedAt(completedAt.toLocalDateTime());
                    }
                    game.setStatus(rs.getString("status"));
                    list.add(game);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}