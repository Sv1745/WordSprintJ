package com.wordsprint.service;

import com.wordsprint.dao.UserDAO;
import com.wordsprint.model.User;
import com.wordsprint.util.PasswordUtil;
import java.time.LocalDateTime;

public class AuthService {
    PasswordUtil pwdUtil = new PasswordUtil();
    public boolean register(String uname, String password, String role) {
        UserDAO dao = new UserDAO();
        User user = dao.findByUsername(uname);
        if (user == null) {
            return dao.createUser(uname, pwdUtil.hashPassword(password), role, LocalDateTime.now());
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
