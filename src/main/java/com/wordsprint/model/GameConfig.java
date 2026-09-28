package com.wordsprint.model;

public class GameConfig {
    private int maxAttempts;
    private int maxDailyGames;
    private boolean gameEnabled;

    public GameConfig() {
        this.maxAttempts = 5;
        this.maxDailyGames = 3;
        this.gameEnabled = true;
    }

    public GameConfig(int maxAttempts, int maxDailyGames, boolean gameEnabled) {
        this.maxAttempts = maxAttempts;
        this.maxDailyGames = maxDailyGames;
        this.gameEnabled = gameEnabled;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getMaxDailyGames() {
        return maxDailyGames;
    }

    public void setMaxDailyGames(int maxDailyGames) {
        this.maxDailyGames = maxDailyGames;
    }

    public boolean isGameEnabled() {
        return gameEnabled;
    }

    public void setGameEnabled(boolean gameEnabled) {
        this.gameEnabled = gameEnabled;
    }
}
