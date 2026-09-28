package com.wordsprint.servlet;

import com.wordsprint.model.Game;
import com.wordsprint.service.GameService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/game")
public class GameServlet extends HttpServlet {

    private final GameService gameService = new GameService();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String action = request.getParameter("action");

        if (action == null) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing action"
            );
            return;
        }

        switch (action) {

            case "start":
                startGame(request, response);
                break;

            case "end":
                endGame(request, response);
                break;

            case "details":
                getGameDetails(request, response);
                break;

            case "config":
                getGameConfig(request, response);
                break;

            default:
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid action"
                );
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        if ("config".equalsIgnoreCase(action)) {
            getGameConfig(request, response);
        } else {
            getGameDetails(request, response);
        }
    }

    private void getGameConfig(HttpServletRequest request, HttpServletResponse response) throws IOException {
        com.wordsprint.dao.AdminDAO adminDAO = new com.wordsprint.dao.AdminDAO();
        com.wordsprint.model.GameConfig config = adminDAO.getConfig();

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(String.format("""
                {
                    "success": true,
                    "maxAttempts": %d,
                    "maxDailyGames": %d,
                    "gameEnabled": %b
                }""", config.getMaxAttempts(), config.getMaxDailyGames(), config.isGameEnabled()));
    }

    private void getGameDetails(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String gameIdParam = request.getParameter("gameId");
        if (gameIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing gameId");
            return;
        }

        try {
            Long gameId = Long.parseLong(gameIdParam);
            com.wordsprint.dao.GameDAO gameDAO = new com.wordsprint.dao.GameDAO();
            com.wordsprint.dao.GuessDAO guessDAO = new com.wordsprint.dao.GuessDAO();

            Game game = gameDAO.findById(gameId);
            if (game == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Game not found");
                return;
            }

            java.util.List<com.wordsprint.model.Guess> guesses = guessDAO.findGuessesByGameId(gameId);

            com.wordsprint.dao.AdminDAO adminDAO = new com.wordsprint.dao.AdminDAO();
            com.wordsprint.model.GameConfig config = adminDAO.getConfig();

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"success\": true,");
            sb.append("\"gameId\": ").append(game.getGameId()).append(",");
            sb.append("\"userId\": ").append(game.getUserId()).append(",");
            sb.append("\"status\": \"").append(game.getStatus()).append("\",");
            sb.append("\"maxAttempts\": ").append(config.getMaxAttempts()).append(",");
            sb.append("\"guesses\": [");
            for (int i = 0; i < guesses.size(); i++) {
                com.wordsprint.model.Guess g = guesses.get(i);
                sb.append("{");
                sb.append("\"guessNumber\": ").append(g.getGuessNumber()).append(",");
                sb.append("\"guessedWord\": \"").append(g.getGuessedWord()).append("\",");
                sb.append("\"result\": \"").append(g.getResult()).append("\"");
                sb.append("}");
                if (i < guesses.size() - 1) sb.append(",");
            }
            sb.append("]");
            sb.append("}");

            response.getWriter().write(sb.toString());

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid gameId");
        }
    }

    private void startGame(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        Long userId = null;
        String userIdParam = request.getParameter("userId");

        if (userIdParam != null) {
            try {
                userId = Long.parseLong(userIdParam);
            } catch (NumberFormatException e) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid userId format"
                );
                return;
            }
        } else {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("user_id") != null) {
                userId = (Long) session.getAttribute("user_id");
            }
        }

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "Please log in first to play WordSprint."
                    }""");
            return;
        }

        com.wordsprint.dao.AdminDAO adminDAO = new com.wordsprint.dao.AdminDAO();
        com.wordsprint.model.GameConfig config = adminDAO.getConfig();

        if (!config.isGameEnabled()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "WordSprint is currently disabled by Admin."
                    }""");
            return;
        }

        com.wordsprint.dao.GameDAO gameDAO = new com.wordsprint.dao.GameDAO();
        if (gameDAO.getGamesCountToday(userId) >= config.getMaxDailyGames()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(String.format("""
                    {
                        "success": false,
                        "message": "Daily limit reached! You can only play a maximum of %d games per day."
                    }""", config.getMaxDailyGames()));
            return;
        }

        Game game = gameService.startGame(userId);

        if (game == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "Unable to start game. Please try again later."
                    }""");
            return;
        }

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write(String.format("""
                {
                    "success": true,
                    "gameId": %d,
                    "userId": %d,
                    "status": "%s",
                    "maxAttempts": %d
                }""", game.getGameId(), game.getUserId(), game.getStatus(), config.getMaxAttempts()));
    }

    private void endGame(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String gameIdParam = request.getParameter("gameId");
        String status = request.getParameter("status");

        if (gameIdParam == null || status == null) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing gameId or status"
            );
            return;
        }

        if (!status.equals("WON") && !status.equals("LOST")) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Status must be WON or LOST"
            );
            return;
        }

        try {
            Long gameId = Long.parseLong(gameIdParam);

            boolean success = gameService.endGame(gameId, status);

            if (!success) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Game not found"
                );
                return;
            }

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("""
                    {
                        "success": true,
                        "message": "Game ended successfully"
                    }""");

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid gameId"
            );
        }
    }
}