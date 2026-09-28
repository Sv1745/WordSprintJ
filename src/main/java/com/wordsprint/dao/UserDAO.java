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

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null) {

            if (con == null || ps == null) {
                return false;
            }

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

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null) {

            if (con == null || ps == null) {
                return null;
            }

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

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(query) : null) {

            if (con == null || ps == null) {
                return null;
            }

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

    public boolean updateUser(Long userId, String newUname, String newPasswordHash) {
        StringBuilder sb = new StringBuilder("UPDATE users SET ");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (newUname != null && !newUname.trim().isEmpty()) {
            sb.append("uname = ?");
            params.add(newUname.trim());
        }

        if (newPasswordHash != null && !newPasswordHash.trim().isEmpty()) {
            if (!params.isEmpty()) sb.append(", ");
            sb.append("pwd_hash = ?");
            params.add(newPasswordHash);
        }

        if (params.isEmpty()) return false;

        sb.append(" WHERE user_id = ?");
        params.add(userId);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sb.toString()) : null) {

            if (con == null || ps == null) return false;

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}