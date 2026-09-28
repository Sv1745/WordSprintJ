package com.wordsprint.service;

import com.wordsprint.dao.GameDAO;
import com.wordsprint.dao.GuessDAO;
import com.wordsprint.dao.WordDAO;
import com.wordsprint.model.Game;
import com.wordsprint.model.Guess;
import com.wordsprint.model.Word;

import java.time.LocalDateTime;

public class GameService {

    private final WordDAO wordDAO = new WordDAO();
    private final GameDAO gameDAO = new GameDAO();
    private final GuessDAO guessDAO = new GuessDAO();

    public Game startGame(Long userId) {
        com.wordsprint.dao.UserDAO userDAO = new com.wordsprint.dao.UserDAO();
        if (userDAO.findById(userId) == null) {
            System.err.println("User ID " + userId + " does not exist.");
            return null;
        }

        if (gameDAO.getGamesCountToday(userId) >= 3) {
            System.err.println("Daily limit reached for user ID " + userId);
            return null;
        }

        Word word = wordDAO.getRandomWord();

        if (word == null) {
            System.err.println("No words available in the database.");
            return null;
        }

        Game game = new Game();

        game.setUserId(userId);
        game.setWordId(word.getWordId());
        game.setStartedAt(LocalDateTime.now());
        game.setStatus("IN_PROGRESS");

        if (!gameDAO.createGame(game)) {
            return null;
        }

        return game;
    }

    public boolean checkAnswer(String guessedWord, String correctWord) {

        if (guessedWord == null || correctWord == null) {
            return false;
        }

        return guessedWord.equalsIgnoreCase(correctWord);
    }

    public boolean recordGuess(
            Long gameId,
            int guessNumber,
            String guessedWord,
            String result
    ) {

        Guess guess = new Guess();

        guess.setGameId(gameId);
        guess.setGuessNumber(guessNumber);
        guess.setGuessedWord(guessedWord);
        guess.setResult(result);
        guess.setCreatedAt(LocalDateTime.now());

        return guessDAO.createGuess(guess);
    }

    public boolean endGame(Long gameId, String status) {

        if (status == null ||
                (!status.equals("WON") && !status.equals("LOST"))) {
            return false;
        }

        return gameDAO.updateGameStatus(gameId, status);
    }
}