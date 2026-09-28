package com.wordsprint.dao;

import com.wordsprint.model.Guess;
import com.wordsprint.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class GuessDAO {

    public boolean createGuess(Guess guess) {

        String query = """
                INSERT INTO guesses
                (game_id, guess_number, guessed_word, result, created_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null) {

            if (con == null || ps == null) {
                return false;
            }

            ps.setLong(1, guess.getGameId());
            ps.setInt(2, guess.getGuessNumber());
            ps.setString(3, guess.getGuessedWord());
            ps.setString(4, guess.getResult());
            ps.setTimestamp(
                    5,
                    Timestamp.valueOf(guess.getCreatedAt())
            );

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<Guess> findGuessesByGameId(Long gameId) {
        java.util.List<Guess> list = new java.util.ArrayList<>();
        String query = """
                SELECT guess_id, game_id, guess_number, guessed_word, result, created_at
                FROM guesses
                WHERE game_id = ?
                ORDER BY guess_number ASC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null) {

            if (con == null || ps == null) return list;

            ps.setLong(1, gameId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Guess guess = new Guess();
                    guess.setGuessId(rs.getLong("guess_id"));
                    guess.setGameId(rs.getLong("game_id"));
                    guess.setGuessNumber(rs.getInt("guess_number"));
                    guess.setGuessedWord(rs.getString("guessed_word"));
                    guess.setResult(rs.getString("result"));
                    guess.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    list.add(guess);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}