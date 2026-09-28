package com.wordsprint.servlet;

import com.wordsprint.model.Game;
import com.wordsprint.service.GameService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

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

            default:
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid action"
                );
        }
    }

    private void startGame(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String userIdParam = request.getParameter("userId");

        if (userIdParam == null) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing userId"
            );
            return;
        }

        try {
            Long userId = Long.parseLong(userIdParam);

            Game game = gameService.startGame(userId);

            if (game == null) {
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Unable to start game"
                );
                return;
            }

            response.setStatus(HttpServletResponse.SC_CREATED);

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid userId"
            );
        }
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

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid gameId"
            );
        }
    }
}