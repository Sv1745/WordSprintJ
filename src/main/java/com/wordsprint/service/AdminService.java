package com.wordsprint.service;

import com.wordsprint.dao.AdminDAO;
import com.wordsprint.model.GameConfig;

import java.util.List;
import java.util.Map;

public class AdminService {

    private final AdminDAO adminDAO = new AdminDAO();

    public Map<String, Integer> getSystemStats() {
        return adminDAO.getSystemStats();
    }

    public List<Map<String, Object>> getPlayerReports() {
        return adminDAO.getPlayerReports();
    }

    public List<Map<String, Object>> getDailyReports() {
        return adminDAO.getDailyReports();
    }

    public List<Map<String, Object>> getUserDailyReports() {
        return adminDAO.getUserDailyReports();
    }

    public GameConfig getGameConfig() {
        return adminDAO.getConfig();
    }

    public boolean updateGameConfig(int maxAttempts, int maxDailyGames, boolean gameEnabled) {
        if (maxAttempts <= 0 || maxAttempts > 10) {
            return false;
        }
        if (maxDailyGames <= 0 || maxDailyGames > 50) {
            return false;
        }

        GameConfig config = new GameConfig(maxAttempts, maxDailyGames, gameEnabled);
        return adminDAO.updateConfig(config);
    }
}
