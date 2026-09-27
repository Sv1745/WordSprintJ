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

    private final Connection con = DBConnection.getConnection();

    public boolean createGame(Game game) {

        String query = """
                INSERT INTO games
                (user_id, word_id, started_at, status)
                VALUES (?, ?, ?, ?)
                """;

        try (
                PreparedStatement ps = con.prepareStatement(
                        query,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

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

        try (
                PreparedStatement ps = con.prepareStatement(query)
        ) {

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

        try (
                PreparedStatement ps = con.prepareStatement(query)
        ) {

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
}