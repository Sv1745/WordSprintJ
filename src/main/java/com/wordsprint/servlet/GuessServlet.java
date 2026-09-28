package com.wordsprint.servlet;

import com.wordsprint.dao.GameDAO;
import com.wordsprint.dao.WordDAO;
import com.wordsprint.model.Game;
import com.wordsprint.model.Word;
import com.wordsprint.service.GameService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/guess")
public class GuessServlet extends HttpServlet {

    private final GameService gameService = new GameService();
    private final GameDAO gameDAO = new GameDAO();
    private final WordDAO wordDAO = new WordDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String gameIdParam = req.getParameter("gameId");
        String guessedWord = req.getParameter("guess");
        if (guessedWord == null) {
            guessedWord = req.getParameter("guessedWord");
        }
        String guessNumParam = req.getParameter("guessNumber");

        if (gameIdParam == null || guessedWord == null) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing gameId or guess parameter");
            return;
        }

        try {
            Long gameId = Long.parseLong(gameIdParam);
            int guessNumber = (guessNumParam != null) ? Integer.parseInt(guessNumParam) : 1;

            Game game = gameDAO.findById(gameId);
            if (game == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "Game not found");
                return;
            }

            Word targetWordObj = wordDAO.findById(game.getWordId());
            if (targetWordObj == null) {
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Target word not found");
                return;
            }

            String targetWord = targetWordObj.getWord().toUpperCase();
            String userGuess = guessedWord.toUpperCase().trim();

            if (userGuess.length() != 5) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Guess must be a 5-letter word");
                return;
            }

            // Calculate feedback result (G = Green/Correct, Y = Yellow/Present, B = Grey/Absent)
            char[] resultChars = new char[5];
            boolean[] targetMatched = new boolean[5];
            boolean[] guessMatched = new boolean[5];

            // 1st pass: Exact matches
            for (int i = 0; i < 5; i++) {
                if (userGuess.charAt(i) == targetWord.charAt(i)) {
                    resultChars[i] = 'G';
                    targetMatched[i] = true;
                    guessMatched[i] = true;
                }
            }

            // 2nd pass: Partial matches or Misses
            for (int i = 0; i < 5; i++) {
                if (!guessMatched[i]) {
                    char c = userGuess.charAt(i);
                    boolean found = false;
                    for (int j = 0; j < 5; j++) {
                        if (!targetMatched[j] && targetWord.charAt(j) == c) {
                            found = true;
                            targetMatched[j] = true;
                            break;
                        }
                    }
                    resultChars[i] = found ? 'Y' : 'B';
                }
            }

            String result = new String(resultChars);
            boolean isCorrect = gameService.checkAnswer(userGuess, targetWord);

            gameService.recordGuess(gameId, guessNumber, userGuess, result);

            String status = "IN_PROGRESS";
            if (isCorrect) {
                status = "WON";
                gameService.endGame(gameId, status);
            } else if (guessNumber >= 5) {
                status = "LOST";
                gameService.endGame(gameId, status);
            }

            res.setContentType("application/json");
            res.setStatus(HttpServletResponse.SC_OK);

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"success\": true,");
            json.append("\"isCorrect\": ").append(isCorrect).append(",");
            json.append("\"result\": \"").append(result).append("\",");
            json.append("\"status\": \"").append(status).append("\"");
            if (isCorrect || "LOST".equals(status)) {
                json.append(",\"targetWord\": \"").append(targetWord).append("\"");
            }
            json.append("}");

            res.getWriter().write(json.toString());

        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid gameId or guessNumber");
        }
    }
}
