package com.wordsprint.service;

import com.wordsprint.dao.UserDAO;
import com.wordsprint.model.User;
import com.wordsprint.util.PasswordUtil;
import java.time.LocalDateTime;

public class AuthService {
    PasswordUtil pwdUtil = new PasswordUtil();
    public String validateCredentials(String uname, String password) {
        if (uname == null || uname.length() < 5) {
            return "Username must be at least 5 characters long.";
        }
        if (!uname.matches(".*[A-Z].*") || !uname.matches(".*[a-z].*")) {
            return "Username must contain both uppercase and lowercase letters.";
        }
        if (password == null || password.length() < 5) {
            return "Password must be at least 5 characters long.";
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            return "Password must contain both letters and numbers.";
        }
        if (!password.matches(".*[$%*@#&!].*")) {
            return "Password must contain at least one special character ($, %, *, @, #, !, &).";
        }
        return null;
    }

    public boolean register(String uname, String password) {
        return register(uname, password, "player");
    }

    public boolean register(String uname, String password, String role) {
        UserDAO dao = new UserDAO();
        User user = dao.findByUsername(uname);
        if (user == null) {
            String dbRole = (role != null) ? role.trim().toLowerCase() : "player";
            if ("user".equals(dbRole)) {
                dbRole = "player";
            }
            if (!"admin".equals(dbRole) && !"player".equals(dbRole)) {
                return false;
            }
            return dao.createUser(uname, pwdUtil.hashPassword(password), dbRole, LocalDateTime.now());
        }
        return false;
    }

    public boolean login(String uname, String password) {
        UserDAO dao = new UserDAO();
        User user = dao.findByUsername(uname);
        if (user == null) {
            return false;
        }
        return (pwdUtil.verifyPassword(password, user.getPasswordHash()));
    }
}
