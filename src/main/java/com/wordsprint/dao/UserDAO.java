package com.wordsprint.dao;

import com.wordsprint.model.User;
import com.wordsprint.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UserDAO {

    private final Connection con = DBConnection.getConnection();

    public boolean createUser(
            String uname,
            String passwordHash,
            String role,
            LocalDateTime createdAt
    ) {

        String query = """
                INSERT INTO users
                (uname, pwd_hash, role, created_at)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, uname);
            ps.setString(2, passwordHash);
            ps.setString(3, role);
            ps.setTimestamp(4, Timestamp.valueOf(createdAt));

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User findByUsername(String uname) {

        String query = """
                SELECT user_id, uname, pwd_hash, role, created_at
                FROM users
                WHERE uname = ?
                """;

        try (
                PreparedStatement ps = con.prepareStatement(query)
        ) {

            ps.setString(1, uname);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    User user = new User();

                    user.setUserId(rs.getLong("user_id"));
                    user.setUserName(rs.getString("uname"));
                    user.setPasswordHash(rs.getString("pwd_hash"));
                    user.setUserRole(rs.getString("role"));
                    user.setCreatedAt(
                            rs.getTimestamp("created_at")
                                    .toLocalDateTime()
                    );

                    return user;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public User findById(Long userId) {

        String query = """
                SELECT user_id, uname, pwd_hash, role, created_at
                FROM users
                WHERE user_id = ?
                """;

        try (
                PreparedStatement ps = con.prepareStatement(query)
        ) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    User user = new User();

                    user.setUserId(rs.getLong("user_id"));
                    user.setUserName(rs.getString("uname"));
                    user.setPasswordHash(rs.getString("pwd_hash"));
                    user.setUserRole(rs.getString("role"));
                    user.setCreatedAt(
                            rs.getTimestamp("created_at")
                                    .toLocalDateTime()
                    );

                    return user;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}