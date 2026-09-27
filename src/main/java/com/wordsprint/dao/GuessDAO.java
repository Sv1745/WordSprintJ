package com.wordsprint.dao;

import com.wordsprint.model.Guess;
import com.wordsprint.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class GuessDAO {

    private final Connection con = DBConnection.getConnection();

    public boolean createGuess(Guess guess) {

        String query = """
                INSERT INTO guesses
                (game_id, guess_number, guessed_word, result, created_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                PreparedStatement ps = con.prepareStatement(query)
        ) {

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
}