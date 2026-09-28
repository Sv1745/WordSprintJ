package com.wordsprint.servlet;

import com.wordsprint.dao.GameDAO;
import com.wordsprint.dao.UserDAO;
import com.wordsprint.model.Game;
import com.wordsprint.model.User;
import com.wordsprint.service.AuthService;
import com.wordsprint.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final GameDAO gameDAO = new GameDAO();
    private final PasswordUtil pwdUtil = new PasswordUtil();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in first");
            return;
        }

        Long userId = (Long) session.getAttribute("user_id");
        User user = userDAO.findById(userId);

        if (user == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }

        List<Game> games = gameDAO.findGamesByUserId(userId);

        int totalGames = games.size();
        int wins = 0;
        int losses = 0;
        int inProgress = 0;

        for (Game g : games) {
            if ("WON".equals(g.getStatus())) wins++;
            else if ("LOST".equals(g.getStatus())) losses++;
            else if ("IN_PROGRESS".equals(g.getStatus())) inProgress++;
        }

        res.setContentType("application/json");
        res.setStatus(HttpServletResponse.SC_OK);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\": true,");
        sb.append("\"userId\": ").append(user.getUserId()).append(",");
        sb.append("\"username\": \"").append(user.getUserName()).append("\",");
        sb.append("\"role\": \"").append(user.getUserRole()).append("\",");
        sb.append("\"createdAt\": \"").append(user.getCreatedAt().toString()).append("\",");
        sb.append("\"stats\": {");
        sb.append("\"totalGames\": ").append(totalGames).append(",");
        sb.append("\"wins\": ").append(wins).append(",");
        sb.append("\"losses\": ").append(losses).append(",");
        sb.append("\"inProgress\": ").append(inProgress);
        sb.append("},");

        sb.append("\"games\": [");
        for (int i = 0; i < games.size(); i++) {
            Game g = games.get(i);
            sb.append("{");
            sb.append("\"gameId\": ").append(g.getGameId()).append(",");
            sb.append("\"status\": \"").append(g.getStatus()).append("\",");
            sb.append("\"startedAt\": \"").append(g.getStartedAt().toString()).append("\"");
            if (g.getCompletedAt() != null) {
                sb.append(",\"completedAt\": \"").append(g.getCompletedAt().toString()).append("\"");
            }
            sb.append("}");
            if (i < games.size() - 1) sb.append(",");
        }
        sb.append("]");
        sb.append("}");

        res.getWriter().write(sb.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in first");
            return;
        }

        Long userId = (Long) session.getAttribute("user_id");
        String newUsername = req.getParameter("username");
        String newPassword = req.getParameter("password");

        String pwdHash = null;
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.length() < 5 || !newPassword.matches(".*[A-Za-z].*") || !newPassword.matches(".*\\d.*") || !newPassword.matches(".*[$%*@#&!].*")) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.setContentType("application/json");
                res.getWriter().write("""
                        {
                            "success": false,
                            "message": "Password must be at least 5 characters and contain letters, numbers, and special chars ($, %, *, @, #, !, &)"
                        }""");
                return;
            }
            pwdHash = pwdUtil.hashPassword(newPassword.trim());
        }

        if (newUsername != null && !newUsername.trim().isEmpty()) {
            String uname = newUsername.trim();
            if (uname.length() < 5 || !uname.matches(".*[A-Z].*") || !uname.matches(".*[a-z].*")) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.setContentType("application/json");
                res.getWriter().write("""
                        {
                            "success": false,
                            "message": "Username must be at least 5 characters and contain uppercase and lowercase letters"
                        }""");
                return;
            }
            User existing = userDAO.findByUsername(uname);
            if (existing != null && !existing.getUserId().equals(userId)) {
                res.setStatus(HttpServletResponse.SC_CONFLICT);
                res.setContentType("application/json");
                res.getWriter().write("""
                        {
                            "success": false,
                            "message": "Username already taken by another user"
                        }""");
                return;
            }
        }

        boolean updated = userDAO.updateUser(userId, newUsername, pwdHash);

        res.setContentType("application/json");
        if (updated) {
            if (newUsername != null && !newUsername.trim().isEmpty()) {
                session.setAttribute("uname", newUsername.trim());
            }
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("""
                    {
                        "success": true,
                        "message": "Profile updated successfully"
                    }""");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("""
                    {
                        "success": false,
                        "message": "No changes made or update failed"
                    }""");
        }
    }
}
