package com.wordsprint.servlet;

import com.wordsprint.model.GameConfig;
import com.wordsprint.service.AdminService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/admin/config")
public class AdminConfigServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access. Please log in.");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (role == null || !"admin".equalsIgnoreCase(role.trim())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Admin role required.");
            return;
        }

        GameConfig config = adminService.getGameConfig();

        res.setContentType("application/json");
        res.setStatus(HttpServletResponse.SC_OK);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\": true,");
        sb.append("\"maxAttempts\": ").append(config.getMaxAttempts()).append(",");
        sb.append("\"maxDailyGames\": ").append(config.getMaxDailyGames()).append(",");
        sb.append("\"gameEnabled\": ").append(config.isGameEnabled());
        sb.append("}");

        res.getWriter().write(sb.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access. Please log in.");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (role == null || !"admin".equalsIgnoreCase(role.trim())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Admin role required.");
            return;
        }

        String attemptsStr = req.getParameter("maxAttempts");
        String dailyStr = req.getParameter("maxDailyGames");
        String enabledStr = req.getParameter("gameEnabled");

        if (attemptsStr == null || dailyStr == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.setContentType("application/json");
            res.getWriter().write("""
                    {
                        "success": false,
                        "message": "Missing maxAttempts or maxDailyGames parameter"
                    }""");
            return;
        }

        try {
            int maxAttempts = Integer.parseInt(attemptsStr.trim());
            int maxDailyGames = Integer.parseInt(dailyStr.trim());
            boolean gameEnabled = (enabledStr != null && ("true".equalsIgnoreCase(enabledStr.trim()) || "on".equalsIgnoreCase(enabledStr.trim())));

            boolean updated = adminService.updateGameConfig(maxAttempts, maxDailyGames, gameEnabled);

            res.setContentType("application/json");
            if (updated) {
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().write("""
                        {
                            "success": true,
                            "message": "Game configuration updated successfully"
                        }""");
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("""
                        {
                            "success": false,
                            "message": "Invalid configuration values. Max attempts (1-10) and Max daily games (1-50) must be positive numbers."
                        }""");
            }
        } catch (NumberFormatException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.setContentType("application/json");
            res.getWriter().write("""
                    {
                        "success": false,
                        "message": "Invalid number format for configuration parameters"
                    }""");
        }
    }
}
