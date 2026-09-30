package com.wordsprint.servlet;

import com.wordsprint.service.AdminService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/reports")
public class AdminReportServlet extends HttpServlet {

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

        Map<String, Integer> stats = adminService.getSystemStats();
        List<Map<String, Object>> reports = adminService.getPlayerReports();
        List<Map<String, Object>> dailyReports = adminService.getDailyReports();
        List<Map<String, Object>> userDailyReports = adminService.getUserDailyReports();
        List<Map<String, Object>> matchReports = adminService.getAllMatchReports();

        res.setContentType("application/json");
        res.setStatus(HttpServletResponse.SC_OK);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\": true,");
        sb.append("\"stats\": {");
        sb.append("\"totalPlayers\": ").append(stats.getOrDefault("totalPlayers", 0)).append(",");
        sb.append("\"totalAdmins\": ").append(stats.getOrDefault("totalAdmins", 0)).append(",");
        sb.append("\"totalGames\": ").append(stats.getOrDefault("totalGames", 0)).append(",");
        sb.append("\"wonGames\": ").append(stats.getOrDefault("wonGames", 0)).append(",");
        sb.append("\"lostGames\": ").append(stats.getOrDefault("lostGames", 0)).append(",");
        sb.append("\"inProgressGames\": ").append(stats.getOrDefault("inProgressGames", 0));
        sb.append("},");

        sb.append("\"reports\": [");
        for (int i = 0; i < reports.size(); i++) {
            Map<String, Object> r = reports.get(i);
            sb.append("{");
            sb.append("\"userId\": ").append(r.get("userId")).append(",");
            sb.append("\"username\": \"").append(r.get("username")).append("\",");
            sb.append("\"role\": \"").append(r.get("role")).append("\",");
            sb.append("\"createdAt\": \"").append(r.get("createdAt")).append("\",");
            sb.append("\"totalGames\": ").append(r.get("totalGames")).append(",");
            sb.append("\"wins\": ").append(r.get("wins")).append(",");
            sb.append("\"losses\": ").append(r.get("losses")).append(",");
            sb.append("\"inProgress\": ").append(r.get("inProgress"));
            sb.append("}");
            if (i < reports.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"dailyReports\": [");
        for (int i = 0; i < dailyReports.size(); i++) {
            Map<String, Object> dr = dailyReports.get(i);
            sb.append("{");
            sb.append("\"date\": \"").append(dr.get("date")).append("\",");
            sb.append("\"activeUsers\": ").append(dr.get("activeUsers")).append(",");
            sb.append("\"correctGuesses\": ").append(dr.get("correctGuesses"));
            sb.append("}");
            if (i < dailyReports.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"userDailyReports\": [");
        for (int i = 0; i < userDailyReports.size(); i++) {
            Map<String, Object> udr = userDailyReports.get(i);
            sb.append("{");
            sb.append("\"username\": \"").append(udr.get("username")).append("\",");
            sb.append("\"date\": \"").append(udr.get("date")).append("\",");
            sb.append("\"wordsTried\": ").append(udr.get("wordsTried")).append(",");
            sb.append("\"correctGuesses\": ").append(udr.get("correctGuesses"));
            sb.append("}");
            if (i < userDailyReports.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"matchReports\": [");
        for (int i = 0; i < matchReports.size(); i++) {
            Map<String, Object> mr = matchReports.get(i);
            sb.append("{");
            sb.append("\"gameId\": ").append(mr.get("gameId")).append(",");
            sb.append("\"username\": \"").append(mr.get("username")).append("\",");
            sb.append("\"word\": \"").append(mr.get("word")).append("\",");
            sb.append("\"startedAt\": \"").append(mr.get("startedAt")).append("\",");
            sb.append("\"completedAt\": \"").append(mr.get("completedAt")).append("\",");
            sb.append("\"status\": \"").append(mr.get("status")).append("\",");
            sb.append("\"attempts\": ").append(mr.get("attempts"));
            sb.append("}");
            if (i < matchReports.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");

        res.getWriter().write(sb.toString());
    }
}
