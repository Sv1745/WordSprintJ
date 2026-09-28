package com.wordsprint.dao;

import com.wordsprint.model.Word;
import com.wordsprint.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WordDAO {

    public Word findById(Long wordId) {

        String query = """
                SELECT word_id, word
                FROM words
                WHERE word_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null) {

            if (con == null || ps == null) {
                return null;
            }

            ps.setLong(1, wordId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Word word = new Word();

                    word.setWordId(rs.getLong("word_id"));
                    word.setWord(rs.getString("word"));

                    return word;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void seedWordsIfEmpty() {
        String countQuery = "SELECT COUNT(*) FROM words";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(countQuery) : null;
             ResultSet rs = ps != null ? ps.executeQuery() : null) {

            if (rs != null && rs.next() && rs.getInt(1) < 20) {
                String[] defaultWords = {
                    "APPLE", "BRAIN", "CHAIR", "DREAM", "EARTH",
                    "FLAME", "GRAPE", "HEART", "IMAGE", "JUICE",
                    "KNIFE", "LIGHT", "MUSIC", "NIGHT", "OCEAN",
                    "PLANT", "QUEEN", "RIGHT", "SMILE", "TRAIN"
                };
                String insertQuery = "INSERT INTO words (word) VALUES (?) ON CONFLICT (word) DO NOTHING";
                try (PreparedStatement insertPs = con.prepareStatement(insertQuery)) {
                    for (String w : defaultWords) {
                        insertPs.setString(1, w);
                        insertPs.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Word getRandomWord() {
        seedWordsIfEmpty();

        String query = """
                SELECT word_id, word
                FROM words
                ORDER BY RANDOM()
                LIMIT 1
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null) {

            if (con == null || ps == null) {
                return null;
            }

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Word word = new Word();

                    word.setWordId(rs.getLong("word_id"));
                    word.setWord(rs.getString("word"));

                    return word;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}