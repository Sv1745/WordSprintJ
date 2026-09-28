package com.wordsprint.util;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

public class DBConnection {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            String url = dotenv.get("DB_URL");
            if (url == null) url = System.getenv("DB_URL");

            String uname = dotenv.get("DB_USERNAME");
            if (uname == null) uname = System.getenv("DB_USERNAME");

            String password = dotenv.get("DB_PASSWORD");
            if (password == null) password = System.getenv("DB_PASSWORD");

            return DriverManager.getConnection(url, uname, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
