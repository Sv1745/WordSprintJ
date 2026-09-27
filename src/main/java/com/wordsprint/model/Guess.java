package com.wordsprint.model;

import java.time.LocalDateTime;

public class Guess {

    private Long guessId;
    private Long gameId;
    private int guessNumber;
    private String guessedWord;
    private String result;
    private LocalDateTime createdAt;

    public Guess() {
    }

    public Guess(Long guessId, Long gameId, int guessNumber,
                 String guessedWord, String result,
                 LocalDateTime createdAt) {

        this.guessId = guessId;
        this.gameId = gameId;
        this.guessNumber = guessNumber;
        this.guessedWord = guessedWord;
        this.result = result;
        this.createdAt = createdAt;
    }

    public Long getGuessId() {
        return guessId;
    }

    public void setGuessId(Long guessId) {
        this.guessId = guessId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public int getGuessNumber() {
        return guessNumber;
    }

    public void setGuessNumber(int guessNumber) {
        this.guessNumber = guessNumber;
    }

    public String getGuessedWord() {
        return guessedWord;
    }

    public void setGuessedWord(String guessedWord) {
        this.guessedWord = guessedWord;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}