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

        Word word = wordDAO.getRandomWord();

        if (word == null) {
            return null;
        }

        Game game = new Game();

        game.setUserId(userId);
        game.setWordId(word.getWordId());
        game.setStartedAt(LocalDateTime.now());
        game.setStatus("IN_PROGRESS");

        gameDAO.createGame(game);

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

    public boolean endGame(Long gameId, boolean won) {
        String status = won ? "WON" : "LOST";
        return gameDAO.updateGameStatus(gameId, status);
    }
}