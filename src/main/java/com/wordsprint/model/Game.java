package com.wordsprint.model;

import java.time.LocalDateTime;

public class Game {

    private Long gameId;
    private Long userId;
    private Long wordId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String status;

    public Game() {
    }

    public Game(Long gameId, Long userId, Long wordId,
                LocalDateTime startedAt,
                LocalDateTime completedAt,
                String status) {

        this.gameId = gameId;
        this.userId = userId;
        this.wordId = wordId;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.status = status;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}