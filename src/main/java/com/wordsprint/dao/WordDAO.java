package com.wordsprint.dao;

import com.wordsprint.model.Word;
import com.wordsprint.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WordDAO {

    private final Connection con = DBConnection.getConnection();

    public Word findById(Long wordId) {

        String query = """
                SELECT word_id, word
                FROM words
                WHERE word_id = ?
                """;

        try (
                PreparedStatement ps = con.prepareStatement(query)
        ) {

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

    public Word getRandomWord() {

        String query = """
                SELECT word_id, word
                FROM words
                ORDER BY RANDOM()
                LIMIT 1
                """;

        try (
                PreparedStatement ps = con.prepareStatement(query);
                ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {

                Word word = new Word();

                word.setWordId(rs.getLong("word_id"));
                word.setWord(rs.getString("word"));

                return word;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}